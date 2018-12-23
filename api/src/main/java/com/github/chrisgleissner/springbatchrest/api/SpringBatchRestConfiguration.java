package com.github.chrisgleissner.springbatchrest.api;

import com.github.chrisgleissner.springbatchrest.api.job.JobController;
import com.github.chrisgleissner.springbatchrest.api.jobdetail.JobDetailController;
import com.github.chrisgleissner.springbatchrest.api.jobexecution.JobExecutionController;
import com.github.chrisgleissner.springbatchrest.util.adhoc.AdHocStarter;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static com.google.common.base.Predicates.not;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static springfox.documentation.builders.PathSelectors.regex;
import static springfox.documentation.spi.DocumentationType.SWAGGER_2;

@Configuration
@EnableBatchProcessing
@EnableSwagger2
@ComponentScan(basePackageClasses= {AdHocStarter.class, JobController.class, JobDetailController.class, JobExecutionController.class })
public class SpringBatchRestConfiguration {

    @Autowired(required = false)
    BuildProperties buildProperties;

    @Bean
    Docket api() {
        return new Docket(SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .paths(not(regex("/error.*")))
                .build().apiInfo(new ApiInfo("Spring Batch REST",
                        "REST API for controlling and viewing <a href=\"https://spring.io/projects/spring-batch\">Spring Batch</a> jobs and their <a href=\"http://www.quartz-scheduler.org\">Quartz</a> schedules.",
                        buildProperties == null ? null : format("%s  -  Build time %s", buildProperties.getVersion(), buildProperties.getTime()),
                        null,
                        new Contact("Github", "https://github.com/chrisgleissner/spring-batch-rest", null),
                        "Apache License 2.0",
                        "http://github.com/chrisgleissner/spring-batch-rest/blob/master/LICENSE", emptyList()));
    }

}
