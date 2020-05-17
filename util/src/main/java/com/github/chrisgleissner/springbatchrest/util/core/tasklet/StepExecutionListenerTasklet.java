package com.github.chrisgleissner.springbatchrest.util.core.tasklet;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.PropertySources;
import org.springframework.core.env.PropertySourcesPropertyResolver;

import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;

import static org.springframework.batch.repeat.RepeatStatus.FINISHED;

@RequiredArgsConstructor
public class StepExecutionListenerTasklet implements Tasklet, StepExecutionListener {
    private final Consumer<StepExecution> stepExecutionConsumer;
    private StepExecution stepExecution;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        stepExecutionConsumer.accept(stepExecution);
        return FINISHED;
    }

    @Override public void beforeStep(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }

    @Override public ExitStatus afterStep(StepExecution stepExecution) {
        return null;
    }
}
