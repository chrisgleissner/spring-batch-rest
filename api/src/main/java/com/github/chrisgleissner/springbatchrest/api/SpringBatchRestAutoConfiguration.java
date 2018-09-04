package com.github.chrisgleissner.springbatchrest.api;


import org.quartz.Scheduler;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnBean({JobExplorer.class, Scheduler.class})
@ComponentScan(basePackageClasses = SpringBatchRestAutoConfiguration.class)
public @interface SpringBatchRestAutoConfiguration {
}
