package com.github.chrisgleissner.springbatchrest.api.jobexecution.provider;

import org.springframework.batch.core.JobExecution;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public interface JobExecutionProvider {

    Collection<JobExecution> getJobExecutions(Optional<String> jobNameRegexp,
                                              Optional<String> exitCode,
                                              int maxNumberOfExecutionsPerJobName);

    default Stream<JobExecution> limit(Stream<JobExecution> s, Optional<String> exitCode, int maxNumberOfExecutionsPerJobName) {
        return s.filter(e ->  exitCode.map(c -> e.getExitStatus().getExitCode().equals(c)).orElse(true))
                .sorted((j1, j2) -> -1 * j1.getEndTime().compareTo(j2.getEndTime()))
                .limit(maxNumberOfExecutionsPerJobName);

    }
}
