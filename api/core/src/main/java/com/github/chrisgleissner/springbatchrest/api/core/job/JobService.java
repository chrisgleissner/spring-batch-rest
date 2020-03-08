package com.github.chrisgleissner.springbatchrest.api.core.job;

import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static java.util.stream.Collectors.toSet;

@Service
public class JobService {
    private final JobRegistry jobRegistry;

    @Autowired
    public JobService(JobRegistry jobRegistry) {
        this.jobRegistry = jobRegistry;
    }

    public Collection<Job> jobs() {
        return jobRegistry.getJobNames().stream().map(n -> new Job(n)).collect(toSet());
    }

    public Job job(String jobName) {
        return new Job(jobName);
    }
}
