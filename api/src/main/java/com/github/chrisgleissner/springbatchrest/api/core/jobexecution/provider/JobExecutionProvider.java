package com.github.chrisgleissner.springbatchrest.api.core.jobexecution.provider;

import org.springframework.batch.core.JobExecution;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

public interface JobExecutionProvider {

    Collection<JobExecution> getJobExecutions(Optional<String> jobNameRegexp,
                                        Optional<String> exitCode,
                                        int limitPerJob);

    default Comparator<JobExecution> byDescendingTime() {
        return (j1, j2) -> {
            int result;
            if (j1.getEndTime() != null && j2.getEndTime() != null)
                result = j1.getEndTime().compareTo(j2.getEndTime());
            else
                result = j1.getStartTime().compareTo(j2.getStartTime());
            return result * -1;
        };
    }
}
