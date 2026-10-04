package com.example.encryptMsg;

import com.example.encryptMsg.service.LLM_InteractiveHoneypotService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EncryptMsgApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(EncryptMsgApplication.class);
		String mode = System.getenv().getOrDefault("APP_MODE", "api");
		if ("worker".equalsIgnoreCase(mode)) {
			app.setWebApplicationType(WebApplicationType.NONE);
			app.setAdditionalProfiles("worker");
		}
		app.run(args);
		if (!"worker".equalsIgnoreCase(mode)) {
			LLM_InteractiveHoneypotService.warmUpLlm();
		}
	}
}
