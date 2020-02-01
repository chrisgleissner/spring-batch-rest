package com.github.chrisgleissner.springbatchrest.api;

import com.github.chrisgleissner.springbatchrest.api.job.JobController;
import com.github.chrisgleissner.springbatchrest.api.jobdetail.JobDetailController;
import com.github.chrisgleissner.springbatchrest.api.jobexecution.JobExecutionController;
import com.github.chrisgleissner.springbatchrest.util.adhoc.AdHocStarter;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableBatchProcessing
@ComponentScan(basePackageClasses= {AdHocStarter.class, JobController.class, JobDetailController.class, JobExecutionController.class })
public class SpringBatchRestApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBatchRestApplication.class, args);
    }
}
