package com.nibado.example.saga.config;

import com.nibado.example.saga.mock.CreditClient;
import com.nibado.example.saga.mock.StockClient;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApplicationConfig {
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public CreditClient creditClient(RestTemplate template) {
        return new CreditClient("http://localhost:8080/credit", template);
    }

    @Bean
    public StockClient stockClient(RestTemplate template) {
        return new StockClient("http://localhost:8080/stock", template);
    }
}
