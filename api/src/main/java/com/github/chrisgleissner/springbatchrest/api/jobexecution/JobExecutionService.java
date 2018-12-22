package com.github.chrisgleissner.springbatchrest.api.jobexecution;

import com.github.chrisgleissner.springbatchrest.api.job.Job;
import com.github.chrisgleissner.springbatchrest.api.jobexecution.provider.CachedJobExecutionProvider;
import com.github.chrisgleissner.springbatchrest.util.adhoc.AdHocStarter;
import com.github.chrisgleissner.springbatchrest.util.adhoc.JobConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.chrisgleissner.springbatchrest.api.jobexecution.JobExecution.fromSpring;

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
        return fromSpring(jobExplorer.getJobExecution(executionId));

    }

    public Collection<JobExecution> jobExecutions(Optional<String> jobNameRegexp,
                                                  Optional<String> exitCode,
                                                  int maxNumberOfExecutionsPerJobName) {
        logger.debug("Getting job executions(jobNameRegexp={}, exitCode={}, maxNumberOfExecutionsPerJobName={})",
                jobNameRegexp, exitCode, maxNumberOfExecutionsPerJobName);
        return jobExecutionProvider.getJobExecutions(jobNameRegexp, exitCode, maxNumberOfExecutionsPerJobName).stream()
                .map(com.github.chrisgleissner.springbatchrest.api.jobexecution.JobExecution::fromSpring)
                .collect(Collectors.toList());

    }

    public JobExecution launch(JobConfig jobConfig) {
        return fromSpring(adHocStarter.start(jobConfig));
    }

    public Job job(String jobName) {
        return new Job(jobName);
    }
}
