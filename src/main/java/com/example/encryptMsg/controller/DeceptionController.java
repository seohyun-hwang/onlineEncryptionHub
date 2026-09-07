package com.example.encryptMsg.controller;

import com.example.encryptMsg.service.LLM_InteractiveHoneypotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/deception")
public class DeceptionController {

    private final LLM_InteractiveHoneypotService honeypotServiceLLM;

    public DeceptionController(LLM_InteractiveHoneypotService honeypotServiceLLM) {
        this.honeypotServiceLLM = honeypotServiceLLM;
    }

    @PostMapping("/admin")
    public ResponseEntity<Map<String, String>> interactWithDecoy(@RequestBody Map<String, String> payload) {
        String userInput = payload.getOrDefault("command", "");
        String aiResponse = honeypotServiceLLM.returnDistractionResponse(userInput);

        return ResponseEntity.ok(Map.of("response", aiResponse));
    }
}