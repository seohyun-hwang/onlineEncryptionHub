package com.example.encryptMsg.serviceTests;

import com.example.encryptMsg.service.TaskScheduleService;
import com.example.encryptMsg.service.TaskScheduleService.TaskRecord;

import org.junit.jupiter.api.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

// ensure that Redis is run, so that this test functions.
// docker compose up -d redis
class RedisScheduleServiceTests {

    private TaskScheduleService scheduler;
    private JedisPool jedisPool;
    private static String host = "localhost";
    private static int port = 6379;

    @BeforeAll
    static void checkRedisAvailability() {
        host = System.getenv().getOrDefault("REDIS_HOST", "127.0.0.1");
        port = Integer.parseInt(System.getenv().getOrDefault("REDIS_PORT", "6379"));

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 1000);
        } catch (IOException e) {
            Assumptions.abort("Redis is unreachable at " + host + ":" + port + ". Skipping integration tests.");
        }
    }

    @BeforeEach
    void setUp() {

        this.jedisPool = new JedisPool(host, port);
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.flushAll();
        }

        this.scheduler = new TaskScheduleService(host, port, "api");
    }

    @AfterEach
    void tearDown() {
        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.close();
        }
    }

    @Test
    @DisplayName("Worker can atomically claim an enqueued task")
    void testEnqueueAndClaim() {
        String taskId = "task-001";
        String payload = "plaintext-data-to-encrypt";

        scheduler.enqueue(taskId, payload);

        Optional<TaskRecord> claimed = scheduler.claimTask("worker-node-A", 2);

        assertThat(claimed).isPresent();
        assertThat(claimed.get().id()).isEqualTo(taskId);
        assertThat(claimed.get().payload()).isEqualTo(payload);

        try (Jedis jedis = jedisPool.getResource()) {
            Map<String, String> data = jedis.hgetAll("task:" + taskId);
            assertThat(data.get("status")).isEqualTo("IN_PROGRESS");
            assertThat(data.get("worker")).isEqualTo("worker-node-A");

            Double score = jedis.zscore("tasks:in_flight", taskId);
            assertThat(score).isNotNull();
            assertThat(score).isGreaterThan(Instant.now().getEpochSecond());
        }
    }

    @Test
    @DisplayName("Completing a task clears in-flight lease and stores output")
    void testCompleteTask() {
        String taskId = "task-002";
        scheduler.enqueue(taskId, "payload-data");
        scheduler.claimTask("worker-node-B", 2);

        scheduler.completeTask(taskId, "ciphertext-result-hash");

        try (Jedis jedis = jedisPool.getResource()) {
            Double score = jedis.zscore("tasks:in_flight", taskId);
            assertThat(score).isNull();

            Map<String, String> data = jedis.hgetAll("task:" + taskId);
            assertThat(data.get("status")).isEqualTo("COMPLETED");
            assertThat(data.get("result")).isEqualTo("ciphertext-result-hash");
        }
    }

    @Test
    @DisplayName("Reaper reclaims tasks when worker exceeds lease duration")
    void testReclaimTimedOutTasks() {
        String taskId = "task-dead-worker";
        scheduler.enqueue(taskId, "sensitive-vector-payload");
        scheduler.claimTask("worker-crashed", 2);

        try (Jedis jedis = jedisPool.getResource()) {
            // Artificially backdate the lease expiry timestamp into the past
            long expiredTimestamp = Instant.now().minusSeconds(10).getEpochSecond();
            jedis.zadd("tasks:in_flight", expiredTimestamp, taskId);
        }

        // Trigger reaper check
        scheduler.reclaimTimedOutTasks();

        try (Jedis jedis = jedisPool.getResource()) {
            assertThat(jedis.zscore("tasks:in_flight", taskId)).isNull();
            assertThat(jedis.hget("task:" + taskId, "status")).isEqualTo("PENDING");
            assertThat(jedis.lrange("tasks:pending", 0, -1)).contains(taskId);
        }

        Optional<TaskRecord> reclaimed = scheduler.claimTask("worker-healthy", 2);
        assertThat(reclaimed).isPresent();
        assertThat(reclaimed.get().id()).isEqualTo(taskId);
    }
}