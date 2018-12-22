package com.github.chrisgleissner.springbatchrest.api.jobexecution.provider;

import com.github.chrisgleissner.springbatchrest.util.adhoc.property.JobExecutionAspect;
import com.google.common.collect.EvictingQueue;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

/**
 * Provides information for recent {@link JobExecution}s and is faster than {@link AllJobExecutionProvider} if a large
 * number of executions exists.
 */
@Slf4j
@Component
public class CachedJobExecutionProvider implements Consumer<JobExecution>, JobExecutionProvider {

    private final int maxNumberOfExecutionsPerJob;
    private final AllJobExecutionProvider allJobExecutionProvider;
    private final Map<String, JobExecutions> jobExecutionsByJobName = new ConcurrentHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public CachedJobExecutionProvider(JobExecutionAspect executionAspect, AllJobExecutionProvider allJobExecutionProvider,
                                      @Value("${com.github.chrisgleissner.springbatchrest.maxNumberOfExecutionsPerJob:100}") int maxNumberOfExecutionsPerJob) {
        executionAspect.register(this);
        this.allJobExecutionProvider = allJobExecutionProvider;
        this.maxNumberOfExecutionsPerJob = maxNumberOfExecutionsPerJob;
    }

    @Override
    public Collection<JobExecution> getJobExecutions(Optional<String> jobNameRegexp, Optional<String> exitCode, int maxNumberOfExecutionsPerJobName) {
        if (maxNumberOfExecutionsPerJobName > this.maxNumberOfExecutionsPerJob)
            return allJobExecutionProvider.getJobExecutions(jobNameRegexp, exitCode, maxNumberOfExecutionsPerJobName);
        else {
            lock.readLock().lock();
            try {
                Optional<Pattern> maybeJobNamePattern = jobNameRegexp.map(r -> Pattern.compile(r));
                Collection<JobExecutions> jobExecutions = jobExecutionsByJobName.entrySet().stream()
                        .filter(e -> maybeJobNamePattern.map(p -> p.matcher(e.getKey()).matches()).orElse(true))
                        .map(e -> e.getValue()).collect(toList());

                Collection<JobExecution> result = new ArrayList<>();
                for (JobExecutions jobExecution : jobExecutions)
                    result.addAll(limit(jobExecution.getJobExecutions(exitCode).stream(), exitCode, maxNumberOfExecutionsPerJobName).collect(toList()));
                return result;
            } finally {
                lock.readLock().unlock();
            }
        }
    }

    @Data
    class JobExecutions {
        private Queue<JobExecution> failedExecutions;
        private Queue<JobExecution> allExecutions;

        JobExecutions() {
            allExecutions = EvictingQueue.create(maxNumberOfExecutionsPerJob);
            failedExecutions = EvictingQueue.create(maxNumberOfExecutionsPerJob);
        }

        Collection<JobExecution> getJobExecutions(Optional<String> exitCode) {
            return exitCode.isPresent() && ExitStatus.FAILED.getExitCode().equals(exitCode.get()) ? failedExecutions : allExecutions;
        }


        void add(JobExecution jobExecution) {
            allExecutions.add(jobExecution);
            if (ExitStatus.FAILED.getExitCode().equals(jobExecution.getExitStatus().getExitCode()))
                failedExecutions.add(jobExecution);
        }
    }

    @Override
    public void accept(JobExecution je) {
        if (!je.isRunning()) {
            lock.writeLock().lock();
            try {
                log.debug("Added JobExecution(id={}, name={}, jobId={}, jobInstanceIdId={}): {}. Details: {}",
                        je.getId(), je.getJobInstance().getJobName(), je.getJobId(), je.getJobInstance().getInstanceId(), je.getExitStatus().getExitCode(), je);
                JobExecutions jes = jobExecutionsByJobName.computeIfAbsent(je.getJobInstance().getJobName(), (n) -> new JobExecutions());
                jes.add(je);
            } finally {
                lock.writeLock().unlock();
            }
        }
    }
}
