package com.github.chrisgleissner.springbatchrest.util.core;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class JobCompletionNotificationListener extends JobExecutionListenerSupport {

    public Semaphore semaphore = new Semaphore(0);

    public void awaitCompletionOfJobs(int numberOfJobs, long maxWaitInMillis) throws InterruptedException {
        if (!semaphore.tryAcquire(numberOfJobs, maxWaitInMillis, TimeUnit.MILLISECONDS)) {
            throw new RuntimeException("Not all jobs have completed. Not completed: " + semaphore.availablePermits());
        }
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            semaphore.release();
        }
    }
}