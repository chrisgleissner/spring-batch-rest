package com.github.chrisgleissner.springbatchrest.util.core;

import com.github.chrisgleissner.springbatchrest.util.core.property.JobPropertyResolvers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import javax.batch.operations.BatchRuntimeException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;

@Slf4j
@Component
public class AdHocStarter {

    private final JobLocator jobLocator;
    private final SimpleJobLauncher asyncJobLauncher;
    private final SimpleJobLauncher syncJobLauncher;
    private final JobPropertyResolvers jobPropertyResolvers;
    private final boolean addUniqueJobParameter;

    public AdHocStarter(JobLocator jobLocator, JobRepository jobRepository, JobPropertyResolvers jobPropertyResolvers,
                        @Value("${com.github.chrisgleissner.springbatchrest.addUniqueJobParameter:true}") boolean addUniqueJobParameter) {
        this.jobLocator = jobLocator;
        asyncJobLauncher = jobLauncher(new SimpleAsyncTaskExecutor(), jobRepository);
        syncJobLauncher = jobLauncher(new SyncTaskExecutor(), jobRepository);
        this.jobPropertyResolvers = jobPropertyResolvers;
        this.addUniqueJobParameter = addUniqueJobParameter;
        log.info("Adding unique job parameter: {}", addUniqueJobParameter);
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

            Map<String, JobParameter> params = Optional.ofNullable(jobConfig.getProperties()).orElse(emptyMap()).entrySet().stream()
                    .collect(toMap(e -> e.getKey(), e -> new JobParameter(e.getValue())));
            if (addUniqueJobParameter)
                params.put("uuid", new JobParameter(UUID.randomUUID().toString()));
            JobParameters jobParameters = new JobParameters(params);

            log.info("Starting {} with {}", jobConfig.getName(), jobConfig);
            JobLauncher jobLauncher = jobConfig.isAsynchronous() ? asyncJobLauncher : syncJobLauncher;
            return jobLauncher.run(job, jobParameters);
        } catch (JobExecutionException e) {
            throw new BatchRuntimeException(format("Failed to start job '%s' with %s. Reason: %s",
                    jobConfig.getName(), jobConfig, e.getMessage()), e);
        } catch (Exception e) {
            throw new RuntimeException(format("Failed to start job '%s' with %s. Reason: %s",
                    jobConfig.getName(), jobConfig, e.getMessage()), e);
        }
    }
}
