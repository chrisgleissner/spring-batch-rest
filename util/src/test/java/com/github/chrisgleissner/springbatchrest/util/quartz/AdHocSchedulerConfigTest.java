package com.github.chrisgleissner.springbatchrest.util.quartz;

import static com.github.chrisgleissner.springbatchrest.util.quartz.QuartzJobLauncher.JOB_LAUNCHER;
import static com.github.chrisgleissner.springbatchrest.util.quartz.QuartzJobLauncher.JOB_LOCATOR;

import org.junit.runner.RunWith;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.chrisgleissner.springbatchrest.util.core.AdHocBatchConfig;
import com.github.chrisgleissner.springbatchrest.util.quartz.AdHocSchedulerConfig;

//@ActiveProfiles("inttest")
//@SpringBootTest(classes = { TestConfig.class, AdHocSchedulerConfig.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@Configuration @ComponentScan
////@RunWith(SpringRunner.class)
////@Configuration @ComponentScan
////@Import(AdHocBatchConfig.class)
//public class AdHocSchedulerConfigTest {
//
////	public static void main(String[] args) {
////        SpringApplication.run(AdHocSchedulerConfigTest.class, args);
////    }
//	
//}
