package com.github.chrisgleissner.springbatchrest.api.quartz;

import com.github.chrisgleissner.springbatchrest.api.quartz.jobdetail.JobDetailController;
import com.github.chrisgleissner.springbatchrest.util.quartz.AdHocScheduler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import static com.github.chrisgleissner.springbatchrest.api.core.Constants.REST_API_ENABLED;

@Configuration
@ConditionalOnClass(name = "org.quartz.Scheduler")
@ConditionalOnProperty(name = REST_API_ENABLED, havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackageClasses= {JobDetailController.class, AdHocScheduler.class})
public class SpringBatchRestQuartzAutoConfiguration {
}
