package com.github.chrisgleissner.springbatchrest.api.core.jobexecution.provider;

import com.github.chrisgleissner.springbatchrest.util.core.property.JobExecutionAspect;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.EvictingQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static com.google.common.collect.ImmutableList.copyOf;

/**
 * Provides information for recent {@link JobExecution}s and is faster than {@link AllJobExecutionProvider} if a large
 * number of executions exists.
 */
@Slf4j
@Component
public class CachedJobExecutionProvider implements Consumer<JobExecution>, JobExecutionProvider {

    private final int cacheSize;
    private final AllJobExecutionProvider allJobExecutionProvider;
    private final Map<String, JobExecutions> jobExecutionsByJobName = new ConcurrentSkipListMap<>(String::compareToIgnoreCase);

    public CachedJobExecutionProvider(JobExecutionAspect executionAspect, AllJobExecutionProvider allJobExecutionProvider,
                                      @Value("${com.github.chrisgleissner.springbatchrest.jobExecutionCacheSize:100}") int jobExecutionCacheSize) {
        executionAspect.register(this);
        this.allJobExecutionProvider = allJobExecutionProvider;
        this.cacheSize = jobExecutionCacheSize;
    }

    @VisibleForTesting
    Map<String, JobExecutions> getJobExecutionsByJobName() {
        return jobExecutionsByJobName;
    }

    @Override
    public Collection<JobExecution> getJobExecutions(Optional<String> jobNameRegexp, Optional<String> exitCode, int limitPerJob) {
        if (limitPerJob > this.cacheSize)
            return allJobExecutionProvider.getJobExecutions(jobNameRegexp, exitCode, limitPerJob);
        else {
            log.debug("Getting job executions from cache for jobNameRegexp={}, exitCode={}, limitPerJob={}", jobNameRegexp, exitCode, limitPerJob);
            Optional<Pattern> maybeJobNamePattern = jobNameRegexp.map(Pattern::compile);
            TreeSet<JobExecution> result = new TreeSet(byDescendingTime());
            jobExecutionsByJobName.entrySet().stream()
                    .filter(e -> maybeJobNamePattern.map(p -> p.matcher(e.getKey()).matches()).orElse(true))
                    .map(Map.Entry::getValue)
                    .flatMap(je -> je.getJobExecutions(exitCode).stream().sorted(byDescendingTime()).limit(limitPerJob))
                    .forEach(result::add);
            log.debug("Found {} job execution(s) for jobNameRegexp={}, exitCode={}, limitPerJob={}", jobNameRegexp, exitCode, limitPerJob, result.size());
            return result;
        }
    }

    class JobExecutions {
        private final Map<String, Queue<JobExecution>> jobExecutionsByExitCode = new HashMap<>();
        private final Queue<JobExecution> jobExecutions = EvictingQueue.create(cacheSize);
        private final ReadWriteLock lock = new ReentrantReadWriteLock();

        Collection<JobExecution> getJobExecutions(Optional<String> exitCode) {
            lock.readLock().lock();
            try {
                return copyOf(exitCode.isPresent() ? jobExecutionsByExitCode.get(exitCode.get()) : this.jobExecutions);
            } finally {
                lock.readLock().unlock();
            }
        }

        void add(JobExecution jobExecution) {
            lock.writeLock().lock();
            try {
                jobExecutionsByExitCode.computeIfAbsent(jobExecution.getExitStatus().getExitCode(),
                        (exitCode) -> EvictingQueue.create(cacheSize)).add(jobExecution);
                jobExecutions.add(jobExecution);
            } finally {
                lock.writeLock().unlock();
            }
        }
    }

    @Override
    public void accept(JobExecution je) {
        if (!je.isRunning()) {
            String jobName = je.getJobInstance().getJobName();
            jobExecutionsByJobName.computeIfAbsent(jobName, (n) -> new JobExecutions()).add(je);
            log.debug("Added JobExecution(id={}, name={}, jobId={}, jobInstanceIdId={}): {}. Details: {}",
                    je.getId(), jobName, je.getJobId(), je.getJobInstance().getInstanceId(), je.getExitStatus().getExitCode(), je);
        }
    }
}
