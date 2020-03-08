package com.github.chrisgleissner.springbatchrest.api.quartz;

import com.github.chrisgleissner.springbatchrest.api.core.job.JobController;
import com.github.chrisgleissner.springbatchrest.api.core.jobexecution.JobExecutionController;
import com.github.chrisgleissner.springbatchrest.api.quartz.jobdetail.JobDetailController;
import com.github.chrisgleissner.springbatchrest.util.core.AdHocStarter;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableBatchProcessing
@ComponentScan(basePackageClasses= {AdHocStarter.class, JobController.class, JobDetailController.class, JobExecutionController.class })
public class SpringBatchRestQuartzTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBatchRestQuartzTestApplication.class, args);
    }
}
