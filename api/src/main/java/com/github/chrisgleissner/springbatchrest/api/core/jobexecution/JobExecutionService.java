package com.github.chrisgleissner.springbatchrest.api.core.jobexecution;

import com.github.chrisgleissner.springbatchrest.api.core.job.Job;
import com.github.chrisgleissner.springbatchrest.api.core.jobexecution.provider.CachedJobExecutionProvider;
import com.github.chrisgleissner.springbatchrest.util.core.AdHocStarter;
import com.github.chrisgleissner.springbatchrest.util.core.JobConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.batch.operations.NoSuchJobExecutionException;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JobExecutionService {

    private final static Logger logger = LoggerFactory.getLogger(JobExecutionService.class);
    private final AdHocStarter adHocStarter;
    private final JobExplorer jobExplorer;
    private final CachedJobExecutionProvider jobExecutionProvider;

    @Autowired
    public JobExecutionService(JobExplorer jobExplorer, CachedJobExecutionProvider jobExecutionProvider, AdHocStarter adHocStarter) {
        this.jobExplorer = jobExplorer;
        this.adHocStarter = adHocStarter;
        this.jobExecutionProvider = jobExecutionProvider;
    }

    public JobExecution jobExecution(long executionId) {
        org.springframework.batch.core.JobExecution jobExecution = jobExplorer.getJobExecution(executionId);
        if (jobExecution == null)
            throw new NoSuchJobExecutionException("Could not find job execution with ID " + executionId);
        return JobExecution.fromSpring(jobExecution);

    }

    public Collection<JobExecution> jobExecutions(Optional<String> jobNameRegexp,
                                                  Optional<String> exitCode,
                                                  int maxNumberOfExecutionsPerJobName) {
        logger.debug("Getting job executions(jobNameRegexp={}, exitCode={}, maxNumberOfExecutionsPerJobName={})",
                jobNameRegexp, exitCode, maxNumberOfExecutionsPerJobName);
        return jobExecutionProvider.getJobExecutions(jobNameRegexp, exitCode, maxNumberOfExecutionsPerJobName).stream()
                .map(JobExecution::fromSpring)
                .collect(Collectors.toList());

    }

    public JobExecution launch(JobConfig jobConfig) {
        return JobExecution.fromSpring(adHocStarter.start(jobConfig));
    }

    public Job job(String jobName) {
        return new Job(jobName);
    }
}
