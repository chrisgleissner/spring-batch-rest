package com.github.chrisgleissner.springbatchrest.util.core.property;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.batch.core.JobExecution;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

@Slf4j
@Aspect
@Component
public class JobExecutionAspect {

    private Set<Consumer<JobExecution>> consumers = new LinkedHashSet<>();

    public void register(Consumer<JobExecution> consumer) {
        consumers.add(consumer);
    }

    @Before("within(org.springframework.batch.core.repository.JobRepository+) && execution(* update(..)) && args(jobExecution)")
    public void jobExecutionUpdated(JobExecution jobExecution) {
        if (jobExecution.getStatus().isUnsuccessful())
            log.error("{} {}: {}", jobExecution.getStatus().name(), jobExecution.getJobInstance().getJobName(), jobExecution);
        else
            log.info("{} {}: {}", jobExecution.getStatus().name(), jobExecution.getJobInstance().getJobName(), jobExecution);
        consumers.forEach(l -> l.accept(jobExecution));
    }
}
