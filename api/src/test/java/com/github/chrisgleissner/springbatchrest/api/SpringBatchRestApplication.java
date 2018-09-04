package com.github.chrisgleissner.springbatchrest.api;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableBatchProcessing
public class SpringBatchRestApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBatchRestApplication.class, args);
    }
}
