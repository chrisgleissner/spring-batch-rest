package com.github.chrisgleissner.springbatchrest.api.jobexecution;

import com.github.chrisgleissner.springbatchrest.util.DateUtil;
import com.google.common.base.Throwables;
import lombok.Builder;
import lombok.Value;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;

import java.time.LocalDateTime;
import java.util.Collection;

import static java.util.stream.Collectors.toList;

@Value
@Builder
public class JobExecution implements Comparable<JobExecution> {

    public static JobExecution fromSpring(String jobName, org.springframework.batch.core.JobExecution je) {
        return JobExecution.builder()
                .jobId(je.getJobId())
                .id(je.getId())
                .jobName(jobName)
                .startTime(DateUtil.localDateTime(je.getStartTime()))
                .endTime(DateUtil.localDateTime(je.getEndTime()))
                .exitStatus(je.getExitStatus())
                .status(je.getStatus())
                .exceptions(je.getFailureExceptions().stream().map(e -> e.getMessage() + ": " + Throwables.getStackTraceAsString(e)).collect(toList()))
                .build();
    }

    private long id;
    private long jobId;
    private String jobName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ExitStatus exitStatus;
    private BatchStatus status;
    private Collection<String> exceptions;


    @Override
    public int compareTo(JobExecution o) {
        int result = this.getJobName() != null ? this.getJobName().compareToIgnoreCase(o.getJobName()) : 0;
        if (result == 0)
            result = id > o.id ? 1 : (id < o.id ? -1 : 0);
        if (result == 0)
            result = jobId > o.jobId ? 1 : (jobId < o.jobId ? -1 : 0);
        return result;
    }
}
