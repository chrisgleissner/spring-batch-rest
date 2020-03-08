package com.github.chrisgleissner.springbatchrest.util.core;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.JobFactory;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.springframework.batch.repeat.RepeatStatus.FINISHED;

@Component
public class JobBuilder {
    private final JobRegistry jobRegistry;
    private final JobBuilderFactory jobs;
    private final StepBuilderFactory steps;

    @Autowired
    public JobBuilder(JobRegistry jobRegistry, JobBuilderFactory jobs, StepBuilderFactory steps) {
        this.jobRegistry = jobRegistry;
        this.jobs = jobs;
        this.steps = steps;
    }

    public Job registerJob(Job job) {
        return registerJob(jobRegistry, job);
    }

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

    public Job createJob(String name, Runnable r) {
        return registerJob(jobs.get(name)
                .incrementer(new RunIdIncrementer())
                .start(steps.get("step").allowStartIfComplete(true).tasklet(new RunnableTaskletAdapter(r)).build()).build());
    }

    private class RunnableTaskletAdapter implements Tasklet {
        private Runnable r;

        public RunnableTaskletAdapter(Runnable r) {
            this.r = r;
        }

        @Override
        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
            r.run();
            return FINISHED;
        }
    }

}
