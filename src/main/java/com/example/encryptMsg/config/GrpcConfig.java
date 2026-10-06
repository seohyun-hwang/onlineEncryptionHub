package com.example.encryptMsg.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!worker")
public class GrpcConfig {
    @Value("${GRPC_CLASSIFIER_HOST}")
    private String host;

    @Value("${GRPC_CLASSIFIER_PORT}")
    private int port;

    @Bean
    public ManagedChannel managedChannel() {
        return ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext() // disabling encryption for local networking
                .build();
    }
}