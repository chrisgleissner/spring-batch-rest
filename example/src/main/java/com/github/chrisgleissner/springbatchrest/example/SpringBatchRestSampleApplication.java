package com.github.chrisgleissner.springbatchrest.example;

import com.github.chrisgleissner.springbatchrest.api.EnableSpringBatchRest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
@EnableSpringBatchRest
public class SpringBatchRestSampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringBatchRestSampleApplication.class, args);
    }
}
