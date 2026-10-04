package com.example.encryptMsg.controller;

import com.example.encryptMsg.service.LLM_InteractiveHoneypotService;
import com.example.encryptMsg.service.TelemetryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.encryptMsg.grpc.BotClassifierGrpc;
import com.example.encryptMsg.grpc.SessionTelemetry;
import com.example.encryptMsg.grpc.PredictionResult;
import io.grpc.ManagedChannel;

import java.util.Map;

@RestController
@RequestMapping("/api/honeypot")
@Profile("!worker")
public class HoneypotController {

    private final BotClassifierGrpc.BotClassifierBlockingStub classifierStub;
    private final TelemetryService telemetryService;
    private final LLM_InteractiveHoneypotService honeypotServiceLLM;

    public HoneypotController(
            ManagedChannel grpcChannel,
            TelemetryService telemetryService,
            LLM_InteractiveHoneypotService honeypotServiceLLM) {
        this.classifierStub = BotClassifierGrpc.newBlockingStub(grpcChannel);
        this.telemetryService = telemetryService;
        this.honeypotServiceLLM = honeypotServiceLLM;
    }

    @PostMapping("/admin")
    public ResponseEntity<Map<String, String>> interactWithHoneypot(
            @RequestBody Map<String, String> payload,
            HttpServletRequest request)
    {
        // 1. Extract the raw string first
        String userInput = payload.getOrDefault("command", "");

        // 2. Pass the extracted string (not the Map) to the telemetry service
        SessionTelemetry telemetry = telemetryService.calculateMetrics(request.getRemoteAddr(), userInput);

        PredictionResult prediction = classifierStub.classifyTraffic(telemetry);

        if (prediction.getIsBot()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // 3. Pass the extracted string to the LLM
        String llmResponse = honeypotServiceLLM.returnDistractionResponse(userInput);

        return ResponseEntity.ok(Map.of("response", llmResponse));
    }
}