package com.github.chrisgleissner.springbatchrest.api.core.jobexecution.provider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.regex.Pattern;

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
        Optional<Pattern> maybeJobNamePattern = jobNameRegexp.map(Pattern::compile);
        List<String> jobNames = jobExplorer.getJobNames().stream()
                .filter(n -> maybeJobNamePattern.map(p -> p.matcher(n).matches()).orElse(true)).collect(toList());
        TreeSet<JobExecution> result = new TreeSet<>(byDescendingTime());
        for (String jobName : jobNames)
            jobExplorer.getJobInstances(jobName, 0, limitPerJob).stream()
                    .flatMap(ji ->jobExplorer.getJobExecutions(ji).stream())
                    .filter(e -> exitCode.map(c -> e.getExitStatus().getExitCode().equals(c)).orElse(true))
                    .sorted(byDescendingTime())
                    .limit(limitPerJob).forEach(result::add);
        log.debug("Found {} job execution(s) for jobNameRegexp={}, exitCode={}, limitPerJob={}",
                jobNameRegexp, exitCode, limitPerJob, result.size());
        return result;
    }
}
