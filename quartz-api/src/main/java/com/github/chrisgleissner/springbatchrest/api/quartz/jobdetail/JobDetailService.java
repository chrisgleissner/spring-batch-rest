package com.github.chrisgleissner.springbatchrest.api.quartz.jobdetail;

import com.github.chrisgleissner.springbatchrest.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.github.chrisgleissner.springbatchrest.util.quartz.QuartzJobLauncher.JOB_NAME;
import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.toList;
import static org.quartz.impl.matchers.GroupMatcher.jobGroupEquals;

@Slf4j
@Service
public class JobDetailService {

    private final Scheduler scheduler;

    @Autowired
    public JobDetailService(Scheduler scheduler) {
        this.scheduler = scheduler;

    }

    public Collection<JobDetail> all(Optional<Boolean> enabled, Optional<String> springBatchJobName) {
        try {
            Set<JobDetail> jobDetails = new TreeSet<>();
            for (String groupName : scheduler.getJobGroupNames()) {
                for (JobKey jobKey : scheduler.getJobKeys(jobGroupEquals(groupName))) {
                    jobDetails.add(jobDetail(jobKey.getGroup(), jobKey.getName()));
                }
            }
            return jobDetails.stream()
                    .filter(d -> enabled.isPresent() ? enabled.get() == d.getNextFireTime().isBefore(now().plusYears(1)) : true)
                    .filter(d -> springBatchJobName.isPresent() ? d.getSpringBatchJobName().get().equals(springBatchJobName.get()) : true)
                    .collect(toList());
        } catch (Exception e) {
            log.error("Couldn't get job details", e);
            throw new RuntimeException("Couldn't get job details", e);
        }
    }

    public JobDetail jobDetail(String quartzGroupName, String quartzJobName) {
        try {
            JobKey jobKey = new JobKey(quartzJobName, quartzGroupName);

            List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
            Trigger trigger = triggers.get(0);
            Date nextFireTime = trigger.getNextFireTime();
            Date previousFireTime = trigger.getPreviousFireTime();

            String springBatchJobName = null;
            JobDataMap jobDataMap = scheduler.getJobDetail(jobKey).getJobDataMap();
            if (jobDataMap != null && jobDataMap.containsKey(JOB_NAME)) {
                springBatchJobName = (String) jobDataMap.get(JOB_NAME);
            }

            String cronExpression = null;
            if (trigger instanceof CronTrigger) {
                cronExpression = ((CronTrigger) trigger).getCronExpression();
            }

            return JobDetail.builder().quartzJobName(quartzJobName).quartzGroupName(quartzGroupName)
                    .springBatchJobName(Optional.ofNullable(springBatchJobName))
                    .nextFireTime(DateUtil.localDateTime(nextFireTime))
                    .previousFireTime(DateUtil.localDateTime(previousFireTime))
                    .cronExpression(Optional.ofNullable(cronExpression)).build();
        } catch (Exception e) {
            log.error("Couldn't get job detail", e);
            throw new RuntimeException(format("Couldn't get job detail for group name '%s' and job name '%s'", quartzGroupName, quartzJobName), e);
        }

    }
}
