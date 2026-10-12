package com.example.encryptMsg.service.honeypot;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class HoneypotTelemetryProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String TOPIC = "honeypot-telemetry";

    public HoneypotTelemetryProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishEvent(String sessionId, String payload) {
        // Send asynchronously to Kafka topic keyed by session ID
        kafkaTemplate.send(TOPIC, sessionId, payload);
    }
}