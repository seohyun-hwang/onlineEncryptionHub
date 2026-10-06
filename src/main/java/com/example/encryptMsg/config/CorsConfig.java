package com.example.encryptMsg.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Profile("!worker")
public class CorsConfig implements WebMvcConfigurer {
    // CORS: Cross-Origin Resource Sharing
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                // Port 3000: Create-React-App default
                // Port 5173: Vite default
                // Port 3000: Next.js default
                .allowedOrigins(
                        "http://localhost:3000",
                        "http://localhost:5173",
                        "http://localhost:3000",
                        "https://online-encryption-hub.vercel.app/"
                )
                .allowedMethods("POST" /*, "GET", "PUT", "DELETE", "OPTIONS"*/)
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}