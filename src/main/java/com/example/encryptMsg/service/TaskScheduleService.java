package com.example.encryptMsg.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.time.Instant;
import java.util.*;

@Service
public class TaskScheduleService { // Redis-based task scheduler

    private final JedisPool pool;
    private final String appMode;

    private static final String QUEUE_PENDING = "tasks:pending";
    private static final String ZSET_IN_FLIGHT = "tasks:in_flight";
    private static final int LEASE_DURATION_SECONDS = 30;

    public TaskScheduleService(
            @Value("${REDIS_HOST:localhost}") String redisHost,
            @Value("${REDIS_PORT:6379}") int redisPort,
            @Value("${APP_MODE:api}") String appMode) {
        this.pool = new JedisPool(redisHost, redisPort);
        this.appMode = appMode;
    }

    public void enqueue(String taskId, String payload) {
        try (Jedis jedis = pool.getResource()) {
            Map<String, String> data = Map.of(
                    "id", taskId,
                    "payload", payload,
                    "status", "PENDING"
            );
            jedis.hset("task:" + taskId, data);
            jedis.lpush(QUEUE_PENDING, taskId);
        }
    }

    public Optional<TaskRecord> claimTask(String workerId, int timeoutSeconds) {
        try (Jedis jedis = pool.getResource()) {
            List<String> popped = jedis.brpop(timeoutSeconds, QUEUE_PENDING);
            if (popped == null || popped.isEmpty()) {
                return Optional.empty();
            }

            String taskId = popped.get(1);
            long leaseExpiry = Instant.now().getEpochSecond() + LEASE_DURATION_SECONDS;

            jedis.zadd(ZSET_IN_FLIGHT, leaseExpiry, taskId);
            jedis.hset("task:" + taskId, Map.of(
                    "status", "IN_PROGRESS",
                    "worker", workerId
            ));

            Map<String, String> details = jedis.hgetAll("task:" + taskId);
            return Optional.of(new TaskRecord(taskId, details.get("payload")));
        }
    }

    public void completeTask(String taskId, String result) {
        try (Jedis jedis = pool.getResource()) {
            jedis.zrem(ZSET_IN_FLIGHT, taskId);
            jedis.hset("task:" + taskId, Map.of(
                    "status", "COMPLETED",
                    "result", result
            ));
        }
    }

    public Optional<Map<String, String>> getTaskStatus(String taskId) {
        try (Jedis jedis = pool.getResource()) {
            Map<String, String> data = jedis.hgetAll("task:" + taskId);
            return data.isEmpty() ? Optional.empty() : Optional.of(data);
        }
    }

    @Scheduled(fixedRate = 10000)
    public void reclaimTimedOutTasks() {
        if (!"api".equalsIgnoreCase(appMode)) return;

        long now = Instant.now().getEpochSecond();
        try (Jedis jedis = pool.getResource()) {
            List<String> expiredTasks = jedis.zrangeByScore(ZSET_IN_FLIGHT, 0, now);
            for (String taskId : expiredTasks) {
                System.out.printf("[Reaper] Lease expired for task %s. Re-enqueuing.%n", taskId);
                jedis.zrem(ZSET_IN_FLIGHT, taskId);
                jedis.hset("task:" + taskId, "status", "PENDING");
                jedis.rpush(QUEUE_PENDING, taskId);
            }
        }
    }

    public record TaskRecord(String id, String payload) {}
}