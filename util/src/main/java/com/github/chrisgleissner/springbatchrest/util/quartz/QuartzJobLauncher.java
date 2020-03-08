package com.github.chrisgleissner.springbatchrest.util.quartz;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.quartz.QuartzJobBean;

@Slf4j
public class QuartzJobLauncher extends QuartzJobBean {

    public static final String JOB_NAME = "jobName";
    public static final String JOB_LOCATOR = "jobLocator";
    public static final String JOB_LAUNCHER = "jobLauncher";

    @Override
    protected void executeInternal(JobExecutionContext context) {
        String jobName = null;
        try {
            JobDataMap dataMap = context.getJobDetail().getJobDataMap();
            jobName = dataMap.getString(JOB_NAME);

            JobLocator jobLocator = (JobLocator) context.getScheduler().getContext().get(JOB_LOCATOR);
            JobLauncher jobLauncher = (JobLauncher) context.getScheduler().getContext().get(JOB_LAUNCHER);

            Job job = jobLocator.getJob(jobName);
            log.info("Starting {}", job.getName());
            JobExecution jobExecution = jobLauncher.run(job, new JobParameters());
            log.info("{}_{} was completed successfully", job.getName(), jobExecution.getId());
        } catch (Exception e) {
            log.error("Job {} failed", jobName, e);
        }
    }
}