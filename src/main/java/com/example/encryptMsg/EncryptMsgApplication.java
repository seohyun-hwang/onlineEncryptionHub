package com.example.encryptMsg;

import com.example.encryptMsg.service.LLM_InteractiveHoneypotService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EncryptMsgApplication {

	public static void main(String[] args) {
		SpringApplication.run(EncryptMsgApplication.class, args);
	}
}
