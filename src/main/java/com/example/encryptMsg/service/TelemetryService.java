package com.example.encryptMsg.service;

import com.example.encryptMsg.grpc.SessionTelemetry;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Profile("!worker")
public class TelemetryService {

    private final JedisPool jedisPool;
    private static final int WINDOW_SECONDS = 30;

    public TelemetryService(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public SessionTelemetry calculateMetrics(String ipAddress, String payload) {
        long now = Instant.now().toEpochMilli();
        long windowStart = now - (WINDOW_SECONDS * 1000);
        String redisKey = "telemetry:ip:" + ipAddress;
        String uniqueMember = now + "-" + UUID.randomUUID().toString();

        double requestRate = 0.0;
        double timeVariance = 0.0;

        try (Jedis jedis = jedisPool.getResource()) {
            // 1. Add current request timestamp
            jedis.zadd(redisKey, now, uniqueMember);

            // 2. Remove old requests outside the 30-second window
            jedis.zremrangeByScore(redisKey, 0, windowStart);
            jedis.expire(redisKey, WINDOW_SECONDS); // Auto-cleanup

            // 3. Fetch remaining timestamps in the window
            List<String> windowRequests = jedis.zrange(redisKey, 0, -1);

            // Calculate Request Rate (requests per second over the window)
            requestRate = (double) windowRequests.size() / WINDOW_SECONDS;

            // Calculate Time Variance (standard deviation of time between requests)
            if (windowRequests.size() > 1) {
                timeVariance = calculateTimeVariance(windowRequests);
            }
        }

        // Calculate Payload Entropy (randomness of the text)
        double payloadEntropy = calculateShannonEntropy(payload);

        // Build and return the gRPC Protobuf object
        return SessionTelemetry.newBuilder()
                .setRequestRate(requestRate)
                .setTimeVariance(timeVariance)
                .setInvalidPathRatio(0.0) // Requires interceptor logic (see below)
                .setPayloadEntropy(payloadEntropy)
                .build();
    }

    private double calculateTimeVariance(List<String> timestamps) {
        long[] times = timestamps.stream()
                .mapToLong(t -> Long.parseLong(t.split("-")[0]))
                .toArray();

        double[] intervals = new double[times.length - 1];
        double sum = 0;
        for (int i = 1; i < times.length; i++) {
            intervals[i - 1] = times[i] - times[i - 1];
            sum += intervals[i - 1];
        }

        double mean = sum / intervals.length;
        double varianceSum = 0;
        for (double interval : intervals) {
            varianceSum += Math.pow(interval - mean, 2);
        }

        return varianceSum / intervals.length;
    }

    private double calculateShannonEntropy(String payload) {
        if (payload == null || payload.isEmpty()) return 0.0;

        Map<Character, Long> charCounts = payload.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.groupingBy(c -> c, Collectors.counting()));

        double entropy = 0.0;
        int length = payload.length();
        for (Long count : charCounts.values()) {
            double probability = (double) count / length;
            entropy -= probability * (Math.log(probability) / Math.log(2));
        }
        return entropy;
    }
}