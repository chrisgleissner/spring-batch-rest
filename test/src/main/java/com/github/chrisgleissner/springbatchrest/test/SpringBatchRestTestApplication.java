package com.github.chrisgleissner.springbatchrest.test;

import com.github.chrisgleissner.springbatchrest.api.annotations.EnableSpringBatchRest;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@SpringBootApplication
@EnableSpringBatchRest
public class SpringBatchRestTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBatchRestTestApplication.class, args);
    }
}
