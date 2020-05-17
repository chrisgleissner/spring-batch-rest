package com.github.chrisgleissner.springbatchrest.util.core.tasklet;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
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
public class PropertyResolverConsumerTasklet implements Tasklet, StepExecutionListener  {
    private final Environment environment;
    private final Consumer<PropertyResolver> propertyResolverConsumer;
    private StepExecution stepExecution;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        propertyResolverConsumer.accept(new PropertySourcesPropertyResolver(propertySources(
                stepExecution.getJobExecution().getJobConfigurationName(),
                stepExecution.getJobParameters().toProperties(), environment)));
        return FINISHED;
    }

    private PropertySources propertySources(String propertyName, Properties properties, Environment env) {
        MutablePropertySources propertySources = new MutablePropertySources();
        if (properties != null)
            propertySources.addFirst(new PropertiesPropertySource(Optional.ofNullable(propertyName).orElse("jobConfig"), properties));
        ((AbstractEnvironment) env).getPropertySources().forEach(propertySources::addLast);
        return propertySources;
    }

    @Override public void beforeStep(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }

    @Override public ExitStatus afterStep(StepExecution stepExecution) {
        return null;
    }
}
