package com.example.api_gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration // Помечаем класс как конфигурационный для Spring
public class webClientConfig {

    // Внедряем значение свойства 'core.service.url' из application.properties
    @Value("${core.service.url}")
    private String coreServiceUrl;

    @Bean // Создаем бин WebClient, который будет доступен для внедрения в другие компоненты
    public WebClient coreServiceClient() {
        return WebClient.builder()
                .baseUrl(coreServiceUrl) // Устанавливаем базовый URL для core-service
                .build();
    }
}
