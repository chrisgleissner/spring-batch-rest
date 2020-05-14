package com.github.chrisgleissner.springbatchrest.util.quartz;

import com.github.chrisgleissner.springbatchrest.util.TriggerUtil;
import com.github.chrisgleissner.springbatchrest.util.core.JobBuilder;
import com.github.chrisgleissner.springbatchrest.util.core.JobConfig;
import com.github.chrisgleissner.springbatchrest.util.core.JobParamsDetail;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.lang.String.format;
import static org.quartz.JobBuilder.newJob;

import java.util.Date;

/**
 * Allows to schedule Spring Batch jobs via Quartz by using a
 * {@link #schedule(String, Job, String)} method rather than Spring wiring each
 * job. This allows for programmatic creation of multiple jobs at run-time.
 */
@Slf4j
@Component
public class AdHocScheduler {

	private final JobBuilder jobBuilder;
	private Scheduler scheduler;
	private JobBuilderFactory jobBuilderFactory;
	private StepBuilderFactory stepBuilderFactory;

	@Autowired
	public AdHocScheduler(JobBuilder jobBuilder, Scheduler scheduler, JobBuilderFactory jobBuilderFactory,
			StepBuilderFactory stepBuilderFactory) {
		this.jobBuilder = jobBuilder;
		this.scheduler = scheduler;
		this.jobBuilderFactory = jobBuilderFactory;
		this.stepBuilderFactory = stepBuilderFactory;
	}

	/**
	 * Schedules a Spring Batch job via a future Date. Job referenced via
	 * jobConfig's name must be a valid registered bean name for a Job object.
	 */
	public synchronized void schedule(JobConfig jobConfig, Date dateToRun) {
		log.debug("Scheduling job {} with custom Trigger", jobConfig.getName());
		try {
			JobDetail jobDetail = this.jobDetailFor(jobConfig);
			Trigger trigger = TriggerUtil.triggerFor(dateToRun, jobConfig.getName());
			scheduler.unscheduleJob(trigger.getKey());
			scheduler.scheduleJob(jobDetail, trigger);
			log.info("Scheduled job {} with Date {}", jobConfig.getName(), dateToRun.toString());
		} catch (Exception e) {
			throw new RuntimeException(
					format("Can't schedule job %s with date: %s", jobConfig.getName(), dateToRun.toString()), e);
		}
	}

	/**
	 * Schedules a Spring Batch job via a Quartz cron expression. Uses the job name
	 * of the provided job.
	 */
	public synchronized Job schedule(Job job, String cronExpression) {
		return this.schedule(job.getName(), job, cronExpression);
	}

	/**
	 * Schedules a Spring Batch job via a Quartz cron expression. Also registers the
	 * job with the specified jobName, rather than the job param's name
	 */
	public synchronized Job schedule(String jobName, Job job, String cronExpression) {
		log.debug("Scheduling job {} with CRON expression {}", jobName, cronExpression);
		try {
			jobBuilder.registerJob(job);
			JobDetail jobDetail = this.jobDetailFor(jobName);

			Trigger trigger = TriggerUtil.triggerFor(cronExpression, jobName);

			scheduler.unscheduleJob(trigger.getKey());
			scheduler.scheduleJob(jobDetail, trigger);
			log.info("Scheduled job {} with CRON expression {}", jobName, cronExpression);
		} catch (Exception e) {
			throw new RuntimeException(format("Can't schedule job %s with cronExpression %s", jobName, cronExpression),
					e);
		}
		return job;
	}

	/**
	 * Schedules a Spring Batch job via a Quartz cron expression. Job referenced via
	 * jobConfig's name must be a valid registered bean name for a Job object.
	 */
	public synchronized void schedule(JobConfig jobConfig, String cronExpression) {
		log.debug("Scheduling job {} with CRON expression {}", jobConfig.getName(), cronExpression);
		try {
			JobDetail jobDetail = this.jobDetailFor(jobConfig);

			Trigger trigger = TriggerUtil.triggerFor(cronExpression, jobConfig.getName());

			scheduler.unscheduleJob(trigger.getKey());
			scheduler.scheduleJob(jobDetail, trigger);
			log.info("Scheduled job {} with CRON expression {}", jobConfig.getName(), cronExpression);
		} catch (Exception e) {
			throw new RuntimeException(
					format("Can't schedule job %s with cronExpression %s", jobConfig.getName(), cronExpression), e);
		}
	}

	/**
	 * Starts the Quartz scheduler unless it is already started. Necessary for any
	 * scheduled jobs to start.
	 */
	public synchronized void start() {
		try {
			if (!scheduler.isStarted()) {
				scheduler.start();
				log.info("Started Quartz scheduler");
			} else {
				log.warn("Quartz scheduler already started");
			}
		} catch (Exception e) {
			throw new RuntimeException("Could not start Quartz scheduler", e);
		}
	}

	public synchronized void pause() {
		try {
			if (scheduler.isStarted() && !scheduler.isInStandbyMode()) {
				scheduler.pauseAll();
				log.info("Paused Quartz scheduler");
			}
		} catch (Exception e) {
			throw new RuntimeException("Could not pause Quartz scheduler", e);
		}
	}

	public synchronized void resume() {
		try {
			if (scheduler.isStarted() && scheduler.isInStandbyMode()) {
				scheduler.resumeAll();
				log.info("Resumed Quartz scheduler");
			}
		} catch (Exception e) {
			throw new RuntimeException("Could not resumse Quartz scheduler", e);
		}
	}

	public synchronized void stop() {
		try {
			if (scheduler.isStarted() && !scheduler.isShutdown()) {
				scheduler.shutdown();
				log.info("Stopped Quartz scheduler");
			}
		} catch (Exception e) {
			throw new RuntimeException("Could not stop Quartz scheduler", e);
		}
	}

	public JobBuilderFactory jobs() {
		return jobBuilderFactory;
	}

	public StepBuilderFactory steps() {
		return stepBuilderFactory;
	}

	// ===============
	// Private Helpers
	// ===============

	private JobDetail jobDetailFor(String jobName) {
		JobConfig config = new JobConfig();
		config.setName(jobName);
		return this.jobDetailFor(config);
	}

	private JobDetail jobDetailFor(JobConfig jobConfig) {
		JobDetail jobDetail = newJob(QuartzJobLauncher.class)
				.withIdentity(jobConfig.getName(), TriggerUtil.QUARTZ_DEFAULT_GROUP)
				.usingJobData(QuartzJobLauncher.JOB_NAME, jobConfig.getName()).build();

		if (jobConfig.getProperties() != null) {
			jobDetail = new JobParamsDetail(jobDetail, jobConfig.getProperties());
		}
		return jobDetail;
	}
}