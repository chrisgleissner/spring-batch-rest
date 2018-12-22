package com.github.chrisgleissner.springbatchrest.api.jobexecution.provider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

@Slf4j
@Component
@RequiredArgsConstructor
public class AllJobExecutionProvider implements JobExecutionProvider {

    private final JobExplorer jobExplorer;

    public Collection<JobExecution> getJobExecutions(Optional<String> jobNameRegexp,
                                                     Optional<String> exitCode,
                                                     int maxNumberOfExecutionsPerJobName) {
        Optional<Pattern> maybeJobNamePattern = jobNameRegexp.map(r -> Pattern.compile(r));
        List<String> jobNames = jobExplorer.getJobNames().stream()
                .filter(n -> maybeJobNamePattern.map(p -> p.matcher(n).matches()).orElse(true)).collect(toList());

        List<JobExecution> allJobExecutions = new ArrayList<>();
        for (String jobName : jobNames) {
            List<JobExecution> jobExecutionsForJob = new ArrayList<>();
            List<JobInstance> jobInstances = jobExplorer.getJobInstances(jobName, 0, maxNumberOfExecutionsPerJobName);
            for (JobInstance jobInstance : jobInstances) {
                List<JobExecution> jobExecutions = jobExplorer.getJobExecutions(jobInstance);
                jobExecutionsForJob.addAll(jobExecutions);
            }
            List<JobExecution> jobExcutionsForJob = limit(jobExecutionsForJob.stream(), exitCode, maxNumberOfExecutionsPerJobName).collect(toList());
            allJobExecutions.addAll(jobExcutionsForJob);
        }
        return allJobExecutions;
    }
}
