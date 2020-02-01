package com.github.chrisgleissner.springbatchrest.api;

import com.github.chrisgleissner.springbatchrest.api.job.JobController;
import com.github.chrisgleissner.springbatchrest.api.jobdetail.JobDetailController;
import com.github.chrisgleissner.springbatchrest.api.jobexecution.JobExecutionController;
import com.github.chrisgleissner.springbatchrest.util.adhoc.AdHocStarter;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
@ComponentScan(basePackageClasses= {AdHocStarter.class, JobController.class, JobDetailController.class, JobExecutionController.class })
public class SpringBatchRestConfiguration {

    @Autowired(required = false)
    BuildProperties buildProperties;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title("Spring Batch REST")
                        .version(buildProperties == null ? null : String.format("%s  -  Build time %s", buildProperties.getVersion(), buildProperties.getTime()))
                        .description("REST API for controlling and viewing <a href=\\\"https://spring.io/projects/spring-batch\\\">" +
                                "Spring Batch</a> jobs and their <a href=\\\"http://www.quartz-scheduler.org\\\">Quartz</a> schedules.")
                        .license(new License().name("Apache License 2.0").url("http://github.com/chrisgleissner/spring-batch-rest/blob/master/LICENSE")));
    }
}
