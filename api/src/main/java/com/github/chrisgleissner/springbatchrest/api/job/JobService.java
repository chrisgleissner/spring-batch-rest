package com.github.chrisgleissner.springbatchrest.api.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.stereotype.Service;
import com.github.chrisgleissner.springbatchrest.api.jobexecution.JobExecution;

import java.util.Collection;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

@Service
public class JobService {

    private final static Logger logger = LoggerFactory.getLogger(JobService.class);

    private JobLocator jobLocator;
    private JobLauncher jobLauncher;
    private JobExplorer jobExplorer;

    @Autowired
    public JobService(JobLocator jobLocator,
                      JobExplorer jobExplorer,
                      JobRepository jobRepository) {
        this.jobLocator = jobLocator;
        this.jobExplorer = jobExplorer;
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        SimpleAsyncTaskExecutor simpleAsyncTaskExecutor = new SimpleAsyncTaskExecutor();
        jobLauncher.setTaskExecutor(simpleAsyncTaskExecutor);
        this.jobLauncher = jobLauncher;
    }

    public Collection<Job> jobs() {
        return jobExplorer.getJobNames().stream().map(n -> new Job(n)).collect(toList());
    }

    public JobExecution launch(String jobName) {
        logger.debug("Launching job {}...", jobName);
        try {
            org.springframework.batch.core.Job job = jobLocator.getJob(jobName);
            try {

                org.springframework.batch.core.JobExecution jobExecution = jobLauncher.run(job, new JobParameters());
                logger.info("Successfully launched job {}: ", jobName, jobExecution);
                return JobExecution.fromSpring(jobName, jobExecution);
            } catch (Exception e) {
                throw new RuntimeException(format("Failed to launch job: %s", jobName), e);
            }
        } catch (NoSuchJobException e) {
            throw new RuntimeException(format("Could not find job: %s", jobName), e);
        }
    }

    public Job job(String jobName) {
        return new Job(jobName);
    }
}
