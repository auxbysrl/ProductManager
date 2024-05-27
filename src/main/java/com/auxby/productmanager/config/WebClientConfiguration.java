package com.auxby.productmanager.config;

import com.auxby.productmanager.config.properties.WebClientProps;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class WebClientConfiguration {

    private final WebClientProps webClientProps;
    @Bean
    public WebClient webClient() {

        return WebClient.builder()
                .baseUrl(webClientProps.getBaseUrl())
                .build();
    }
}
