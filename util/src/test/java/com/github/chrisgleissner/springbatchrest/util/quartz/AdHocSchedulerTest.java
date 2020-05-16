package com.github.chrisgleissner.springbatchrest.util.quartz;

import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.chrisgleissner.springbatchrest.util.core.JobBuilder;
import com.github.chrisgleissner.springbatchrest.util.core.JobConfig;
import com.github.chrisgleissner.springbatchrest.util.quartz.config.AdHocSchedulerConfig;

import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.time.Instant;
import java.util.Date;

/**
 * Tests the ad-hoc Quartz scheduling of Spring Batch jobs, allowing for
 * programmatic scheduling after Spring wiring.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = AdHocSchedulerConfig.class)
public class AdHocSchedulerTest {

	private static final String TRIGGER_EVERY_SECOND = "0/1 * * * * ?";
	private static final int NUMBER_OF_EXECUTIONS_PER_JOB = 2;

	@Autowired
	private AdHocScheduler scheduler;

	@Autowired
	private JobBuilder jobBuilder;

	private CountDownLatch latch1 = new CountDownLatch(NUMBER_OF_EXECUTIONS_PER_JOB);
	private CountDownLatch latch2 = new CountDownLatch(NUMBER_OF_EXECUTIONS_PER_JOB);

	@BeforeEach
	public void before() {
		latch1 = new CountDownLatch(NUMBER_OF_EXECUTIONS_PER_JOB);
		latch2 = new CountDownLatch(NUMBER_OF_EXECUTIONS_PER_JOB);
	}

	@AfterAll
	public void afterAll() {
		scheduler.stop();
	}

	@Test
	public void scheduleCronWithJobReferenceWorks() throws InterruptedException {
		scheduler.schedule("j1", job("j1", latch1), TRIGGER_EVERY_SECOND);
		scheduler.schedule("j2", job("j2", latch2), TRIGGER_EVERY_SECOND);
		scheduler.start();

		latch1.await(4, SECONDS);
		latch2.await(4, SECONDS);
		scheduler.pause();
	}

	@Test
	public void scheduleWithJobConfigAndDateWorks() throws InterruptedException {
		Job job1 = job("j1", latch1);
		Job job2 = job("j2", latch2);

		jobBuilder.registerJob(job1);
		jobBuilder.registerJob(job2);

		JobConfig job1Config = JobConfig.builder().name("j1").build();
		JobConfig job2Config = JobConfig.builder().name("j2").build();

		Date oneSecondFromNow = Date.from(Instant.now().plusMillis(1000));

		scheduler.schedule(job1Config, oneSecondFromNow);
		scheduler.schedule(job2Config, oneSecondFromNow);
		scheduler.start();

		latch1.await(4, SECONDS);
		latch2.await(4, SECONDS);
		scheduler.pause();
	}

	@Test
	public void scheduleWithJobConfigAndCronWorks() throws InterruptedException {
		Job job1 = job("j1", latch1);
		Job job2 = job("j2", latch2);

		jobBuilder.registerJob(job1);
		jobBuilder.registerJob(job2);

		JobConfig job2Config = JobConfig.builder().name("j2").build();

		scheduler.schedule("j1", job1, TRIGGER_EVERY_SECOND);
		scheduler.schedule(job2Config, TRIGGER_EVERY_SECOND);
		scheduler.start();

		latch1.await(4, SECONDS);
		latch2.await(4, SECONDS);
		scheduler.pause();
	}

	@Test
	public void happyCaseSchedulerStartPauseResumeNoThrow() {
		Assertions.assertDoesNotThrow(() -> {
			scheduler.start();
		});
		Assertions.assertDoesNotThrow(() -> {
			scheduler.pause();
		});
		Assertions.assertDoesNotThrow(() -> {
			scheduler.resume();
		});
		// Future - handle & test the case where the scheduler has been shutdown and
		// needs re-initialization
		// https://stackoverflow.com/questions/15020625/quartz-how-to-shutdown-and-restart-the-scheduler
	}

	@Test
	public void exceptionForBadJobConfigDate() {
		Assertions.assertThrows(RuntimeException.class, () -> {
			scheduler.schedule(JobConfig.builder().name(null).asynchronous(false).build(), new Date());
		});
	}

	@Test
	public void exceptionForBadJobConfigCron() {
		Assertions.assertThrows(RuntimeException.class, () -> {
			scheduler.schedule(JobConfig.builder().name(null).asynchronous(false).build(), TRIGGER_EVERY_SECOND);
		});
	}

	@Test
	public void exceptionForBadParamsCron() {
		Assertions.assertThrows(RuntimeException.class, () -> {
			scheduler.schedule(null, null, TRIGGER_EVERY_SECOND);
		});
	}

	private Job job(String jobName, CountDownLatch latch) {
		return scheduler.jobs().get(jobName).incrementer(new RunIdIncrementer()) // adds unique parameter on each run so
																					// that createJob can be rerun
				.start(scheduler.steps().get("step").tasklet((contribution, chunkContext) -> {
					latch.countDown();
					return RepeatStatus.FINISHED;
				}).allowStartIfComplete(true).build()).build();
	}
}
