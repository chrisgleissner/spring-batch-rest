package com.github.chrisgleissner.springbatchrest.util.adhoc;

import com.github.chrisgleissner.springbatchrest.util.adhoc.property.JobPropertyResolvers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import static java.util.stream.Collectors.toMap;

@Slf4j
@Component
public class AdHocStarter {

    private final JobLocator jobLocator;
    private final SimpleJobLauncher asyncJobLauncher;
    private final SimpleJobLauncher syncJobLauncher;
    private final JobPropertyResolvers jobPropertyResolvers;

    public AdHocStarter(JobLocator jobLocator, JobRepository jobRepository, JobPropertyResolvers jobPropertyResolvers) {
        this.jobLocator = jobLocator;
        asyncJobLauncher = jobLauncher(new SimpleAsyncTaskExecutor(), jobRepository);
        syncJobLauncher = jobLauncher(new SyncTaskExecutor(), jobRepository);
        this.jobPropertyResolvers = jobPropertyResolvers;
    }

    private SimpleJobLauncher jobLauncher(TaskExecutor taskExecutor, JobRepository jobRepository) {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(taskExecutor);
        return jobLauncher;
    }

    public JobExecution start(JobConfig jobConfig) {
        try {
            Job job = jobLocator.getJob(jobConfig.getName());
            jobPropertyResolvers.started(jobConfig);
            JobParameters jobParameters = new JobParameters(jobConfig.getProperties().entrySet().stream()
                    .collect(toMap(e -> e.getKey(), e -> new JobParameter(e.getValue()))));
            log.info("Starting {} with {}", jobConfig.getName(), jobConfig);
            JobLauncher jobLauncher = jobConfig.isAsynchronous() ? asyncJobLauncher : syncJobLauncher;
            return jobLauncher.run(job, jobParameters);
        } catch (Exception e) {
            throw new RuntimeException("Failed to start " + jobConfig.getName(), e);
        }
    }
}
