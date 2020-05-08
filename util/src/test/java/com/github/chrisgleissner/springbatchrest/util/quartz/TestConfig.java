package com.github.chrisgleissner.springbatchrest.util.quartz;

import static com.github.chrisgleissner.springbatchrest.util.quartz.QuartzJobLauncher.JOB_LAUNCHER;
import static com.github.chrisgleissner.springbatchrest.util.quartz.QuartzJobLauncher.JOB_LOCATOR;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

//@Configuration
//@ComponentScan
//public class TestConfig {
//	
//	@MockBean
//	public JobLauncher jobLauncher;
//	
//    @Bean
//    @Primary
//    public Scheduler scheduler(JobLocator jobLocator, JobLauncher jobLauncher) throws SchedulerException {
//        Scheduler scheduler = new StdSchedulerFactory().getScheduler();
//        scheduler.getContext().put(JOB_LOCATOR, jobLocator);
//        scheduler.getContext().put(JOB_LAUNCHER, jobLauncher);
//        return scheduler;
//    }
//}
