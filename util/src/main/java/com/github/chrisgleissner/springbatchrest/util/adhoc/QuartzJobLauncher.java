package com.github.chrisgleissner.springbatchrest.util.adhoc;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.quartz.QuartzJobBean;

import static org.slf4j.LoggerFactory.getLogger;

public class QuartzJobLauncher extends QuartzJobBean {

    public static final String JOB_NAME = "jobName";
    public static final String JOB_LOCATOR = "jobLocator";
    public static final String JOB_LAUNCHER = "jobLauncher";

    private static final Logger logger = getLogger(QuartzJobLauncher.class);

    @Override
    protected void executeInternal(JobExecutionContext context) {
        try {
            JobDataMap dataMap = context.getJobDetail().getJobDataMap();
            String jobName = dataMap.getString(JOB_NAME);

            JobLocator jobLocator = (JobLocator) context.getScheduler().getContext().get(JOB_LOCATOR);
            JobLauncher jobLauncher = (JobLauncher) context.getScheduler().getContext().get(JOB_LAUNCHER);

            Job job = jobLocator.getJob(jobName);
            logger.info("Starting job {}", job.getName());
            JobExecution jobExecution = jobLauncher.run(job, new JobParameters());
            logger.info("{}_{} was completed successfully", job.getName(), jobExecution.getId());
        } catch (Exception e) {
            logger.error("Encountered job execution exception", e);
        }
    }
}