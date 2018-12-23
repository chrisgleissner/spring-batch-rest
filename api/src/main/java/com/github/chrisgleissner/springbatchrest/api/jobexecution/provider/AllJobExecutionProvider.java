package com.github.chrisgleissner.springbatchrest.api.jobexecution.provider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

import static java.util.Collections.sort;
import static java.util.stream.Collectors.toList;

@Slf4j
@Component
@RequiredArgsConstructor
public class AllJobExecutionProvider implements JobExecutionProvider {

    private final JobExplorer jobExplorer;

    public Collection<JobExecution> getJobExecutions(Optional<String> jobNameRegexp,
                                                     Optional<String> exitCode,
                                                     int limitPerJob) {

        log.debug("Getting job executions from JobExplorer for jobNameRegexp={}, exitCode={}, limitPerJob={}", jobNameRegexp, exitCode, limitPerJob);
        Optional<Pattern> maybeJobNamePattern = jobNameRegexp.map(r -> Pattern.compile(r));
        List<String> jobNames = jobExplorer.getJobNames().stream()
                .filter(n -> maybeJobNamePattern.map(p -> p.matcher(n).matches()).orElse(true)).collect(toList());

        List<JobExecution> allJobExecutions = new ArrayList<>();
        for (String jobName : jobNames) {
            List<JobExecution> jobExecutionsForJob = new ArrayList<>();
            List<JobInstance> jobInstances = jobExplorer.getJobInstances(jobName, 0, limitPerJob);
            for (JobInstance jobInstance : jobInstances) {
                List<JobExecution> jobExecutions = jobExplorer.getJobExecutions(jobInstance);
                jobExecutionsForJob.addAll(jobExecutions);
            }

            List<JobExecution> jobExcutionsForJob = jobExecutionsForJob.stream()
                    .filter(e -> exitCode.map(c -> e.getExitStatus().getExitCode().equals(c)).orElse(true))
                    .sorted(byDescendingTime())
                    .limit(limitPerJob).collect(toList());
            allJobExecutions.addAll(jobExcutionsForJob);
        }
        sort(allJobExecutions, byDescendingTime());
        log.debug("Found {} job execution(s) for jobNameRegexp={}, exitCode={}, limitPerJob={}",
                jobNameRegexp, exitCode, limitPerJob, allJobExecutions.size());

        return allJobExecutions;
    }
}
