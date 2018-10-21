package com.github.chrisgleissner.springbatchrest.api.job;

import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static java.util.stream.Collectors.toList;

@Service
public class JobService {

    private JobExplorer jobExplorer;

    @Autowired
    public JobService(JobExplorer jobExplorer) {
        this.jobExplorer = jobExplorer;
    }

    public Collection<Job> jobs() {
        return jobExplorer.getJobNames().stream().map(n -> new Job(n)).collect(toList());
    }

    public Job job(String jobName) {
        return new Job(jobName);
    }
}
