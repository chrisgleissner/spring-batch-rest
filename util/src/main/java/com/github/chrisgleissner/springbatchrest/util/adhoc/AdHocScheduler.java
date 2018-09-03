package com.github.chrisgleissner.springbatchrest.util.adhoc;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.DuplicateJobException;
import org.springframework.batch.core.configuration.JobFactory;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

import static java.lang.String.format;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Allows to schedule Spring Batch jobs via Quartz by using a {@link #schedule(String, Supplier, String)} method
 * rather than Spring wiring each job. This allows for programmatic creation of multiple jobs at run-time.
 */
@Component
public class AdHocScheduler {

    private static final Logger logger = getLogger(AdHocScheduler.class);

    private static final String GROUP_NAME = "group";

    private JobRegistry jobRegistry;
    private Scheduler scheduler;
    private JobBuilderFactory jobBuilderFactory;
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    public AdHocScheduler(JobRegistry jobRegistry,Scheduler scheduler, JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobRegistry = jobRegistry;
        this.scheduler = scheduler;
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    /**
     * Schedules a Spring Batch job via a Quartz cron expression.
     */
    public void schedule(String jobName, Supplier<Job> jobSupplier, String cronExpression) throws DuplicateJobException {
        logger.debug("Scheduling job {} with CRON expression {}", jobName, cronExpression);
        jobRegistry.register(new JobFactory() {
            @Override
            public Job createJob() {
                return jobSupplier.get();
            }

            @Override
            public String getJobName() {
                return jobName;
            }
        });

        try {
            JobDetail jobDetail = newJob(QuartzJobLauncher.class)
                    .withIdentity(jobName, GROUP_NAME)
                    .usingJobData(QuartzJobLauncher.JOB_NAME, jobName)
                    .build();

            Trigger trigger = newTrigger()
                    .withIdentity(jobName + "-trigger", GROUP_NAME)
                    .withSchedule(cronSchedule(cronExpression))
                    .forJob(jobName, GROUP_NAME)
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);
            logger.info("Scheduled job {} with CRON expression {}", jobName, cronExpression);
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
                logger.info("Started Quartz scheduler");
            } else {
                logger.warn("Quartz scheduler already started");
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not start Quartz scheduler", e);
        }
    }

    public JobBuilderFactory jobs() {
        return jobBuilderFactory;
    }

    public StepBuilderFactory steps() {
        return stepBuilderFactory;
    }
}