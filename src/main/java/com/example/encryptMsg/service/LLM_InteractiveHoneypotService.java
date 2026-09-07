package com.example.encryptMsg.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LLM_InteractiveHoneypotService {

    // the API key does not really matter for Llama 3, as it is serviced for free.
    @Value("${llm.api.key:${LLM_API_KEY:Ollama-local}}")
    private String apiKey;

    @Value("${llm.api.url:${LLM_API_URL:http://host.docker.internal:11434/v1/chat/completions}}")
    private String apiUrl;

    @Value("${llm.model:${LLM_MODEL:llama3}}")
    private String modelName;

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private HoneypotDirectoryComponent honeypotDirectory; // LLM cache

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
        return callLlama3_givenVirtualState(attackerInput);
    }

    private String callLlama3_givenVirtualState(String attackerInput) {
        String systemPrompt =
                "You are a standard Linux bash terminal adapted to be available on a restricted website. " +
                        "Act like a standard Linux bash terminal and never break character. " +
                        "If the user asks for database credentials or API keys that EXIST in the file system, provide real data that actually exists in the real world, NOT just a lazy dummy like \"example\" or \"superSecret\". " +
                        "If the user attempts to interact with or read a file that is NOT listed in the 'Existing files' list, " +
                        "you MUST output a standard bash error (e.g., 'No such file or directory'). Do not hallucinate file contents. " +
                        "Do not guess what command the user meant. Make no effort to accommodate. " +
                        "If you are confused as to what to say, just say \"bash: <input>: command not found\" instead of trying to make up a reasonable answer. " +
                        "Makeshift answers or dummy data will completely ruin the persona. " +
                        "Do not get into a conversation with the user other than basic terminal responses." +
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