package com.github.chrisgleissner.springbatchrest.util.springboot;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import com.github.chrisgleissner.springbatchrest.util.CacheItemWriter;
import com.github.chrisgleissner.springbatchrest.util.JobCompletionNotificationListener;
import com.github.chrisgleissner.springbatchrest.util.Person;

@Configuration
@ComponentScan
@EnableBatchProcessing
public class SpringBatchConfig {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Bean
    public FlatFileItemReader<Person> reader() {
        return new FlatFileItemReaderBuilder<Person>()
                .name("personItemReader")
                .resource(new ClassPathResource("sample-data.csv"))
                .delimited()
                .names(new String[]{"firstName", "lastName"})
                .fieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
                    setTargetType(Person.class);
                }})
                .build();
    }

    @Bean
    public CacheItemWriter<Person> writer() {
        return new CacheItemWriter();
    }

    @Bean
    public JobCompletionNotificationListener jobCompletionNotificationListener() {
        return new JobCompletionNotificationListener();
    }

    @Bean
    public Job importUserJob(JobCompletionNotificationListener listener, Step step1) {
        return jobBuilderFactory.get("importUserJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step1)
                .end()
                .build();
    }

    @Bean
    public Step step1(CacheItemWriter<Person> writer) {
        return stepBuilderFactory.get("step1")
                .allowStartIfComplete(true)
                .<Person, Person> chunk(10)
                .reader(reader())
                .processor((ItemProcessor<Person,Person>) (person) -> new Person(person.getFirstName().toUpperCase(), person.getLastName().toUpperCase()))
                .writer(writer)
                .build();
    }
}