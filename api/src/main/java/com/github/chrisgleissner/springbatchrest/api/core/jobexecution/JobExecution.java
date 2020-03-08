package com.github.chrisgleissner.springbatchrest.api.core.jobexecution;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.chrisgleissner.springbatchrest.util.DateUtil;
import com.google.common.base.Throwables;
import lombok.Builder;
import lombok.Value;
import org.springframework.batch.core.BatchStatus;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;

import static java.util.stream.Collectors.toList;

@Value
@Builder
public class JobExecution implements Comparable<JobExecution> {

    private static final String EXIT_CODE = "exitCode";
    private static final String EXIT_DESCRIPTION = "exitDescription";

    public static JobExecution fromSpring(org.springframework.batch.core.JobExecution je) {
        return JobExecution.builder()
                .jobId(je.getJobId())
                .id(je.getId())
                .jobName(je.getJobInstance().getJobName())
                .startTime(DateUtil.localDateTime(je.getStartTime()))
                .endTime(DateUtil.localDateTime(je.getEndTime()))
                .exitCode(je.getExitStatus() == null ? null : je.getExitStatus().getExitCode())
                .exitDescription(je.getExitStatus() == null ? null : je.getExitStatus().getExitDescription())
                .status(je.getStatus())
                .exceptions(je.getFailureExceptions().stream().map(e -> e.getMessage() + ": " + Throwables.getStackTraceAsString(e)).collect(toList()))
                .build();
    }

    private long id;
    private long jobId;
    private String jobName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String exitCode;
    private String exitDescription;
    @JsonSerialize(using = BatchStatusSerializer.class)
    private BatchStatus status;

    private Collection<String> exceptions;

    @Override
    public int compareTo(JobExecution o) {
        int result = this.getJobName() != null ? this.getJobName().compareToIgnoreCase(o.getJobName()) : 0;
        if (result == 0)
            result = Long.compare(id, o.id);
        if (result == 0)
            result = Long.compare(jobId, o.jobId);
        return result;
    }

    static class BatchStatusSerializer extends JsonSerializer<BatchStatus> {
        @Override
        public void serialize(BatchStatus batchStatus, JsonGenerator jsonGen, SerializerProvider serializerProvider) throws IOException {
            jsonGen.writeString(batchStatus.name());
        }
    }
}
