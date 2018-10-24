package com.github.chrisgleissner.springbatchrest.api.jobexecution;

import com.github.chrisgleissner.springbatchrest.api.job.Job;
import com.github.chrisgleissner.springbatchrest.util.adhoc.AdHocStarter;
import com.github.chrisgleissner.springbatchrest.util.adhoc.JobConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

import static com.github.chrisgleissner.springbatchrest.api.jobexecution.JobExecution.fromSpring;
import static java.lang.Integer.MAX_VALUE;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Service
public class JobExecutionService {

    private final static Logger logger = LoggerFactory.getLogger(JobExecutionService.class);
    private final AdHocStarter adHocStarter;
    private final JobExplorer jobExplorer;

    @Autowired
    public JobExecutionService(JobExplorer jobExplorer, AdHocStarter adHocStarter) {
        this.jobExplorer = jobExplorer;
        this.adHocStarter = adHocStarter;
    }

    public JobExecution jobExecution(long executionId) {
        return fromSpring(jobExplorer.getJobExecution(executionId));

    }

    public Collection<JobExecution> jobExecutions(Optional<String> jobNameRegexp,
                                                  Optional<ExitStatus> exitStatus,
                                                  Optional<Integer> maxNumberOfJobInstances,
                                                  Optional<Integer> maxNumberOfJobExecutionsPerInstance) {
        logger.debug("Getting createJob excecutions(jobNameRegexp={}, exitStatus={}, maxNumberOfJobInstances{}, maxNumberOfJobExecutionsPerInstance={}",
                jobNameRegexp, exitStatus, maxNumberOfJobInstances, maxNumberOfJobExecutionsPerInstance);

        Set<JobExecution> allJobExecutions = new TreeSet<>();
        List<String> jobNames = jobExplorer.getJobNames();
        if (jobNameRegexp.isPresent()) {
            Pattern p = Pattern.compile(jobNameRegexp.get());
            jobNames = jobNames.stream().filter(jn -> p.matcher(jn).matches()).collect(toList());
        }

        for (String jobName : jobNames) {
            try {
                int jobInstanceCount = jobExplorer.getJobInstanceCount(jobName);
                List<JobInstance> jobInstances = jobExplorer.getJobInstances(jobName,
                        Math.max(0, jobInstanceCount - maxNumberOfJobInstances.orElse(MAX_VALUE)),
                        maxNumberOfJobInstances.orElse(MAX_VALUE));
                for (JobInstance jobInstance : jobInstances) {
                    List<JobExecution> jobExecutions = jobExplorer.getJobExecutions(jobInstance).stream()
                            .limit(maxNumberOfJobExecutionsPerInstance.orElse(MAX_VALUE))
                            .map(JobExecution::fromSpring)
                            .collect(toList());
                    allJobExecutions.addAll(jobExecutions);
                }
            } catch (Exception e) {
                logger.warn("Could not get executions for job {}", jobName, e);
            }
        }
        return allJobExecutions.stream().filter(je -> !exitStatus.isPresent() || exitStatus.get().equals(je.getExitStatus())).collect(toSet());
    }

    public JobExecution launch(JobConfig jobConfig) {
        return fromSpring(adHocStarter.start(jobConfig));
    }

    public Job job(String jobName) {
        return new Job(jobName);
    }
}
