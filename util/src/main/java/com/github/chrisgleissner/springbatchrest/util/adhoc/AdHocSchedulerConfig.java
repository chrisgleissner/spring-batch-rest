package com.github.chrisgleissner.springbatchrest.util.adhoc;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

import static com.github.chrisgleissner.springbatchrest.util.adhoc.QuartzJobLauncher.JOB_LAUNCHER;
import static com.github.chrisgleissner.springbatchrest.util.adhoc.QuartzJobLauncher.JOB_LOCATOR;

@Configuration
@ComponentScan
@EnableBatchProcessing
public class AdHocSchedulerConfig extends DefaultBatchConfigurer {

    @Autowired
    private JobLocator jobLocator;

    @Autowired
    private JobLauncher jobLauncher;

    @Override
    public void setDataSource(DataSource dataSource) {
        // override to do not set datasource even if a datasource exist.
        // initialize will use a Map based JobRepository (instead of database)
    }

    @Bean
    public Scheduler scheduler() throws SchedulerException {
        Scheduler scheduler = new StdSchedulerFactory().getScheduler();
        scheduler.getContext().put(JOB_LOCATOR, jobLocator);
        scheduler.getContext().put(JOB_LAUNCHER, jobLauncher);
        return scheduler;
    }
}
