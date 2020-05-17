package com.github.chrisgleissner.springbatchrest.util.core;

import com.github.chrisgleissner.springbatchrest.util.core.tasklet.PropertyResolverConsumerTasklet;
import com.github.chrisgleissner.springbatchrest.util.core.tasklet.RunnableTasklet;
import com.github.chrisgleissner.springbatchrest.util.core.tasklet.StepExecutionListenerTasklet;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.JobFactory;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component @RequiredArgsConstructor
public class JobBuilder {
    private final JobRegistry jobRegistry;
    private final JobBuilderFactory jobs;
    private final StepBuilderFactory steps;
    private final Environment environment;

    public static Job registerJob(JobRegistry jobRegistry, Job job) {
        jobRegistry.unregister(job.getName());
        try {
            jobRegistry.register(new JobFactory() {
                @Override
                public Job createJob() {
                    return job;
                }

                @Override
                public String getJobName() {
                    return job.getName();
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Could not create " + job.getName(), e);
        }
        return job;
    }

    public Job registerJob(Job job) {
        return registerJob(jobRegistry, job);
    }

    public Job createJob(String name, Runnable runnable) {
        return createJob(name, new RunnableTasklet(runnable));
    }

    private Job createJob(String name, Tasklet tasklet) {
        return registerJob(jobs.get(name).incrementer(new RunIdIncrementer())
                .start(steps.get("step").allowStartIfComplete(true).tasklet(tasklet).build()).build());
    }

    public Job createJob(String name, Consumer<PropertyResolver> propertyResolverConsumer) {
        return createJob(name, new PropertyResolverConsumerTasklet(environment, propertyResolverConsumer));
    }

    public Job createJobFromStepExecutionConsumer(String name, Consumer<StepExecution> stepExecutionConsumer) {
        return createJob(name, new StepExecutionListenerTasklet(stepExecutionConsumer));
    }
}
