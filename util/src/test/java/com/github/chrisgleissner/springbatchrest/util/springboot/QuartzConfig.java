package com.github.chrisgleissner.springbatchrest.util.springboot;

import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class QuartzConfig {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobLocator jobLocator;

    @Bean
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor(JobRegistry jobRegistry) {
        JobRegistryBeanPostProcessor postProcessor = new JobRegistryBeanPostProcessor();
        postProcessor.setJobRegistry(jobRegistry);
        return postProcessor;
    }

    @Bean
    public JobDetailFactoryBean jobDetailFactoryBean() {
        JobDetailFactoryBean factory = new JobDetailFactoryBean();
        factory.setJobClass(QuartzJobLauncher.class);
        Map map = new HashMap();
        map.put("jobName", "importUserJob");
        map.put("jobLauncher", jobLauncher);
        map.put("jobLocator", jobLocator);
        factory.setJobDataAsMap(map);
        return factory;
    }

    @Bean
    public CronTriggerFactoryBean cronTriggerFactoryBean() {
        CronTriggerFactoryBean triggerFactory = new CronTriggerFactoryBean();
        triggerFactory.setJobDetail(jobDetailFactoryBean().getObject());
        triggerFactory.setCronExpression("0/1 * * * * ?");
        return triggerFactory;
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean scheduler = new SchedulerFactoryBean();
        scheduler.setTriggers(cronTriggerFactoryBean().getObject());
        return scheduler;
    }
}