package com.github.chrisgleissner.springbatchrest.util.quartz;

import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.MethodMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.chrisgleissner.springbatchrest.util.JobParamUtil;
import com.github.chrisgleissner.springbatchrest.util.core.JobBuilder;
import com.github.chrisgleissner.springbatchrest.util.core.JobConfig;
import com.github.chrisgleissner.springbatchrest.util.quartz.config.AdHocSchedulerConfig;
import com.github.chrisgleissner.springbatchrest.util.quartz.AdHocSchedulerParamsTest.CustomContextConfiguration;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.github.chrisgleissner.springbatchrest.util.quartz.QuartzJobLauncher.JOB_LAUNCHER;
import static com.github.chrisgleissner.springbatchrest.util.quartz.QuartzJobLauncher.JOB_LOCATOR;

/**
 * Tests the ad-hoc Quartz scheduling of Spring Batch jobs with parameters
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(
	classes = { 
		AdHocSchedulerConfig.class,
		CustomContextConfiguration.class 
	}, 
	name = "mockJobLauncherContext")
public class AdHocSchedulerParamsTest {

	private static final String TRIGGER_EVERY_SECOND = "0/1 * * * * ?";

	@Autowired
	private AdHocScheduler scheduler;

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private JobBuilder jobBuilder;

	@AfterAll
	public void afterAll() {
		scheduler.stop();
	}

	// Override the scheduler's job launcher with a mock so we can verify
	// that calls to it contain parameters
	protected static class CustomContextConfiguration {

		@MockBean
		public JobLauncher mockJobLauncher;

		@Bean
		@Primary
		public Scheduler scheduler(JobLocator jobLocator) throws SchedulerException {
			Scheduler scheduler = new StdSchedulerFactory().getScheduler();
			scheduler.getContext().remove(JOB_LOCATOR);
			scheduler.getContext().put(JOB_LOCATOR, jobLocator);
			scheduler.getContext().remove(JOB_LAUNCHER);
			scheduler.getContext().put(JOB_LAUNCHER, mockJobLauncher);
			return scheduler;
		}
	}

	@Test
	@DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
	public void paramsAddedToScheduledJobWorks()
			throws InterruptedException, JobExecutionAlreadyRunningException, JobRestartException,
			JobInstanceAlreadyCompleteException, JobParametersInvalidException, SchedulerException {

		Job job1 = job("j1");
		Job job2 = job("j2");

		jobBuilder.registerJob(job1);
		jobBuilder.registerJob(job2);

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("testParamKey", "testParamValue");

		JobParameters expectedParams = JobParamUtil.convertRawToJobParams(params);

		JobConfig job1Config = JobConfig.builder().name("j1").properties(params).build();
		JobConfig job2Config = JobConfig.builder().name("j2").properties(params).build();

		Date now = Date.from(Instant.now().plusMillis(2000));

		scheduler.start();

		when(jobLauncher.run(job1, expectedParams))
				.thenReturn(new JobExecution(new Random().nextLong(), expectedParams));

		when(jobLauncher.run(job2, expectedParams))
				.thenReturn(new JobExecution(new Random().nextLong(), expectedParams));

		scheduler.schedule(job1Config, now);
		scheduler.schedule(job2Config, TRIGGER_EVERY_SECOND);

		ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
		ArgumentCaptor<JobParameters> jobParamCaptor = ArgumentCaptor.forClass(JobParameters.class);
		verify(jobLauncher, timeout(8000).times(2)).run(jobCaptor.capture(), jobParamCaptor.capture());

		List<JobParameters> paramsListAfterCall = jobParamCaptor.getAllValues();

		Assertions.assertEquals(paramsListAfterCall.size(), 2);

		for (JobParameters jobParams : paramsListAfterCall) {
			Assertions.assertEquals(jobParams.getString("testParamKey"), "testParamValue");
		}

		scheduler.pause();
	}

	private Job job(String jobName) {
		return scheduler.jobs().get(jobName).incrementer(new RunIdIncrementer()) // adds unique parameter on each run so
																					// that createJob can be rerun
				.start(scheduler.steps().get("step").tasklet((contribution, chunkContext) -> {
					return RepeatStatus.FINISHED;
				}).allowStartIfComplete(true).build()).build();
	}
}
