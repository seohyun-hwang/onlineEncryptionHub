package com.example.encryptMsg.service.honeypot;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class HoneypotTelemetryConsumer {

    @KafkaListener(topics = "honeypot-telemetry", groupId = "honeypot-workers")
    public void consumeEvent(String payload) {
        System.out.println("Ingested telemetry event: " + payload);
    }
}