package com.example.encryptMsg.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcConfig {
    @Value("${GRPC_CLASSIFIER_HOST:localhost}")
    private String host;

    @Value("${GRPC_CLASSIFIER_PORT:50051}")
    private int port;

    @Bean
    public ManagedChannel managedChannel() {
        return ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext() // disabling encryption for local networking
                .build();
    }
}