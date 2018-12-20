package com.github.chrisgleissner.springbatchrest.util.adhoc;

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
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Allows to schedule Spring Batch jobs via Quartz by using a {@link #schedule(String, Job, String)} method
 * rather than Spring wiring each job. This allows for programmatic creation of multiple jobs at run-time.
 */
@Slf4j
@Component
public class AdHocScheduler {
    private static final String GROUP_NAME = "group";

    private final JobBuilder jobBuilder;
    private Scheduler scheduler;
    private JobBuilderFactory jobBuilderFactory;
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    public AdHocScheduler(JobBuilder jobBuilder, Scheduler scheduler, JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilder = jobBuilder;
        this.scheduler = scheduler;
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    /**
     * Schedules a Spring Batch job via a Quartz cron expression.
     */
    public void schedule(String jobName, Job job, String cronExpression) {
        log.debug("Scheduling job {} with CRON expression {}", jobName, cronExpression);
        try {
            jobBuilder.registerJob(job);
            JobDetail jobDetail = newJob(QuartzJobLauncher.class)
                    .withIdentity(jobName, GROUP_NAME)
                    .usingJobData(QuartzJobLauncher.JOB_NAME, jobName)
                    .build();

            Trigger trigger = newTrigger()
                    .withIdentity(jobName + "-trigger", GROUP_NAME)
                    .withSchedule(cronSchedule(cronExpression))
                    .forJob(jobName, GROUP_NAME)
                    .build();

            scheduler.unscheduleJob(trigger.getKey());
            scheduler.scheduleJob(jobDetail, trigger);
            log.info("Scheduled job {} with CRON expression {}", jobName, cronExpression);
        } catch (Exception e) {
            throw new RuntimeException(format("Can't schedule job %s with cronExpression %s", jobName, cronExpression), e);
        }
    }

    /**
     * Starts the Quartz scheduler unless it is already started. Necessary for any scheduled jobs to start.
     */
    public void start() {
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

    public void pause() {
        try {
            scheduler.pauseAll();
            log.info("Paused Quartz scheduler");
        } catch (Exception e) {
            throw new RuntimeException("Could not pause Quartz scheduler", e);
        }
    }

    public void resume() {
        try {
            scheduler.resumeAll();
            log.info("Resumed Quartz scheduler");
        } catch (Exception e) {
            throw new RuntimeException("Could not resumse Quartz scheduler", e);
        }
    }

    public void stop() {
        try {
            scheduler.shutdown();
            log.info("Stopped Quartz scheduler");
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
}