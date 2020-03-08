package com.github.chrisgleissner.springbatchrest.api.quartz.jobdetail;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.Optional;

@Value
@Builder
public class JobDetail implements Comparable<JobDetail> {
    private String quartzJobName;
    private String quartzGroupName;
    private Optional<String> springBatchJobName;
    private String description;
    private Optional<String> cronExpression;
    private LocalDateTime nextFireTime;
    private LocalDateTime previousFireTime;

    @Override
    public int compareTo(JobDetail o) {
        int result = 0;
        if (nextFireTime != null && nextFireTime.isBefore(o.nextFireTime))
            result = -1;
        else if (nextFireTime != null && nextFireTime.isAfter(o.nextFireTime))
            result = 1;
        if (result == 0)
            result = quartzGroupName.compareToIgnoreCase(o.quartzGroupName);
        if (result == 0)
            result = quartzJobName.compareToIgnoreCase(o.quartzJobName);
        return result;
    }
}
