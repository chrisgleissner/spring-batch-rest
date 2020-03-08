package com.github.chrisgleissner.springbatchrest.util.quartz;

import com.github.chrisgleissner.springbatchrest.util.core.AdHocBatchConfig;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static com.github.chrisgleissner.springbatchrest.util.quartz.QuartzJobLauncher.JOB_LAUNCHER;
import static com.github.chrisgleissner.springbatchrest.util.quartz.QuartzJobLauncher.JOB_LOCATOR;

@Configuration @ComponentScan
@Import(AdHocBatchConfig.class)
public class AdHocSchedulerConfig {

    @Bean
    public Scheduler scheduler(JobLocator jobLocator, JobLauncher jobLauncher) throws SchedulerException {
        Scheduler scheduler = new StdSchedulerFactory().getScheduler();
        scheduler.getContext().put(JOB_LOCATOR, jobLocator);
        scheduler.getContext().put(JOB_LAUNCHER, jobLauncher);
        return scheduler;
    }
}
