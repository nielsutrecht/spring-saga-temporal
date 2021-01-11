package com.nibado.example.saga;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpringSagaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringSagaApplication.class, args);
    }

}
