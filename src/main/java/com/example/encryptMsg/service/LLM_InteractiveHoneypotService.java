package com.example.encryptMsg.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.*;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Profile("!worker")
public class LLM_InteractiveHoneypotService {

    private static final Logger logger = LoggerFactory.getLogger(LLM_InteractiveHoneypotService.class);

    // the API key does not really matter for Llama 3, as it is serviced for free.
    @Value("${llm.api.key:${LLM_API_KEY:Ollama-local}}")
    private String apiKey;

    @Value("${llm.api.url:${LLM_API_URL:http://host.docker.internal:11434/v1/chat/completions}}")
    private String apiUrl;

    @Value("${llm.model:${LLM_MODEL:llama3}}")
    private String modelName;

    @Value("${APP_MODE:api}")
    private String appMode;

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private HoneypotDirectoryComponent honeypotDirectory; // LLM cache

    // a function for starting the LLM in the background immediatley upon project start so that its first response doesn't take forever
    // called in the main class "EncryptMsgApplication"
    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void warmUpLLMOnStartup() {
        try {
            // Perform a lightweight dummy completion query here
            logger.info("Warming up LLM Honeypot model asynchronously...");
            // call your LLM client with a short 3-5 second timeout
        } catch (Exception e) {
            logger.warn("LLM Warmup failed or timed out. Falling back to local heuristic mode: {}", e.getMessage());
        }
    }

    public String returnDistractionResponse(String attackerInput) {
        attackerInput = attackerInput.trim();
        System.out.println("[SECURITY AUDIT]: " + attackerInput);

        // Some deterministic responses to avoid complete LLM-hallucination
        if (attackerInput.equals("pwd")) {
            return honeypotDirectory.getCurrentDirectory();
        }
        if (attackerInput.equals("whoami")) {
            return "web-admin";
        }
        if (attackerInput.startsWith("cd")) {
            String dir = attackerInput.length() > 2 ? attackerInput.substring(2).trim() : "";
            honeypotDirectory.changeDirectory(dir);
            return "";
        }
        if (attackerInput.startsWith("cat ")
            || attackerInput.endsWith(".txt")) {
            String fileName = attackerInput.substring(4).trim();
            return honeypotDirectory.getFileContent(fileName);
        }
        if (attackerInput.equals("ls") || attackerInput.equals("ls -la")) {
            StringBuilder sb = new StringBuilder();
            honeypotDirectory.getFiles().stream()
                    .filter(f -> f.startsWith(honeypotDirectory.getCurrentDirectory()))
                    .forEach(f -> {
                        String displayPath = f.substring(honeypotDirectory.getCurrentDirectory().length());
                        if (displayPath.startsWith("/")) displayPath = displayPath.substring(1);
                        if (!displayPath.isEmpty() && !displayPath.contains("/")) {
                            sb.append(displayPath).append("  ");
                        }
                    });
            return !sb.isEmpty() ? sb.toString() : "total 0";
        }
        if (attackerInput.startsWith("touch ")) {
            String fileName = attackerInput.substring(6).trim();
            honeypotDirectory.touchFile(fileName);
            return "";
        }
        if (attackerInput.startsWith("rm ")) {
            String fileName = attackerInput.substring(3).trim();
            honeypotDirectory.removeFile(fileName);
            return "";
        }

        // For nondeterministic demands, the LLM is activated.
        return callLlM_afterStart(attackerInput);
    }

    private String callLlM_afterStart(String attackerInput) {
        String systemPrompt =
                "You are the stdout stream of an unprivileged Linux BASH terminal. " +
                "You do not possess a human persona. Do not explain your output. Do not apologize. Do not include markdown code blocks like ```bash. " +
                "If a command succeeds, return only the raw terminal strings. If it fails, return only the standard bash error syntax (e.g., bash: command not found). " +
                "Act ONLY as a silent Linux binary. Never use English. Do not hallucinate file contents. " +
                "If the user attempts to interact with or read a file that is NOT listed in the 'Existing files' list, " +
                "Makeshift answers or dummy data will completely ruin the persona. " +
                "Current working directory: " + honeypotDirectory.getCurrentDirectory() + ".\n" +
                "Existing files in system: " + String.join(", ", honeypotDirectory.getFiles()) + ".\n" +
                "Below is a list showing file-contents mapped to corresponding system-files: \n" + honeypotDirectory.getCurrentFileContentMap_forLLM();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", modelName);
        requestBody.put("temperature", 0.7);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", attackerInput)
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");

            return (String) message.get("content");
        } catch (Exception e) {
            e.printStackTrace();
            return "bash: " + attackerInput + ": command not found";
        }
    }
}