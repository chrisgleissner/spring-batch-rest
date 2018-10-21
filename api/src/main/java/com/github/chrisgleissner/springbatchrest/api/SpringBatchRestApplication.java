package com.github.chrisgleissner.springbatchrest.api;

import com.github.chrisgleissner.springbatchrest.api.job.JobController;
import com.github.chrisgleissner.springbatchrest.api.jobdetail.JobDetailController;
import com.github.chrisgleissner.springbatchrest.api.jobexecution.JobExecutionController;
import com.github.chrisgleissner.springbatchrest.util.adhoc.AdHocStarter;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Controller;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableBatchProcessing
@EnableSwagger2
@ComponentScan(basePackageClasses= {AdHocStarter.class, JobController.class, JobDetailController.class, JobExecutionController.class })
public class SpringBatchRestApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBatchRestApplication.class, args);
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build();
    }
}
