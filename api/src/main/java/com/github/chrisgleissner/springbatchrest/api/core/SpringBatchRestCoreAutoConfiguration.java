package com.github.chrisgleissner.springbatchrest.api.core;

import com.github.chrisgleissner.springbatchrest.api.core.job.JobController;
import com.github.chrisgleissner.springbatchrest.api.core.jobexecution.JobExecutionController;
import com.github.chrisgleissner.springbatchrest.util.core.AdHocStarter;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import static com.github.chrisgleissner.springbatchrest.api.core.Constants.REST_API_ENABLED;

@Configuration
@EnableBatchProcessing
@ConditionalOnProperty(name = REST_API_ENABLED, havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackageClasses= {AdHocStarter.class, JobController.class, JobExecutionController.class })
public class SpringBatchRestCoreAutoConfiguration {

    @Autowired(required = false)
    BuildProperties buildProperties;

    @Bean
    @ConditionalOnMissingBean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title("Spring Batch REST")
                        .version(buildProperties == null ? null : String.format("%s  -  Build time %s", buildProperties.getVersion(), buildProperties.getTime()))
                        .description("REST API for controlling and viewing <a href=\"https://spring.io/projects/spring-batch\">" +
                                "Spring Batch</a> jobs and their <a href=\"http://www.quartz-scheduler.org\">Quartz</a> schedules.")
                        .license(new License().name("Apache License 2.0").url("http://github.com/chrisgleissner/spring-batch-rest/blob/master/LICENSE")));
    }
}
