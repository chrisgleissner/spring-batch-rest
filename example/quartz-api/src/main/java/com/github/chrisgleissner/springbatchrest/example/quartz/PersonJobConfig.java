package com.github.chrisgleissner.springbatchrest.example.quartz;

import com.github.chrisgleissner.springbatchrest.util.quartz.AdHocScheduler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.function.FunctionItemProcessor;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.synchronizedList;

@Configuration @RequiredArgsConstructor @Slf4j
public class PersonJobConfig {
    static final String JOB_NAME = "personJob";
    static final String LAST_NAME_PREFIX = "lastNamePrefix";

    private final JobBuilderFactory jobs;
    private final StepBuilderFactory steps;
    private final AdHocScheduler adHocScheduler;
    private final Environment environment;

    @Bean
    Job personJob(@Qualifier("personStep") Step personStep) {
        return adHocScheduler.schedule(PersonJobConfig.JOB_NAME, jobs.get(JOB_NAME)
                .incrementer(new RunIdIncrementer())
                .flow(personStep)
                .end()
                .build(), "0 0 12 * * ?");
    }

    @Bean
    Step personStep(@Qualifier("personProcessor") ItemProcessor personProcessor) {
        return steps.get("personStep")
                .allowStartIfComplete(true)
                .<Person, Person>chunk(3)
                .reader(personReader())
                .processor(personProcessor)
                .writer(personWriter())
                .build();
    }

    @Bean
    FlatFileItemReader<Person> personReader() {
        return new FlatFileItemReaderBuilder<Person>()
                .name("personItemReader")
                .resource(new ClassPathResource("person.csv"))
                .delimited()
                .names(new String[]{"firstName", "lastName"})
                .fieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
                    setTargetType(Person.class);
                }})
                .build();
    }

    @Bean @StepScope
    ItemProcessor personProcessor(
            @Qualifier("personNameCaseChange") ItemProcessor personNameCaseChange,
            @Value("#{jobParameters['" + LAST_NAME_PREFIX + "']}") String lastNamePrefix) {
        CompositeItemProcessor p = new CompositeItemProcessor();
        p.setDelegates(newArrayList(
                personNameFilter(Optional.ofNullable(lastNamePrefix).orElseGet(() -> environment.getProperty(LAST_NAME_PREFIX))),
                personNameCaseChange));
        return p;
    }

    private ItemProcessor personNameFilter(String lastNamePrefix) {
        return new FunctionItemProcessor<Person, Person>(p -> {
            log.info("Last name prefix: {}", lastNamePrefix);
            return p.lastName != null && p.lastName.startsWith(lastNamePrefix) ? p : null;
        });
    }

    @Bean @StepScope
    ItemProcessor personNameCaseChange(@Value("#{jobParameters['upperCase']}") Boolean upperCaseParam) {
        boolean upperCase = upperCaseParam == null ? false : upperCaseParam;
        log.info("personNameCaseChange(upperCase={})", upperCase);
        return new FunctionItemProcessor<Person, Person>(p -> new Person(
                upperCase ? p.firstName.toUpperCase() : p.firstName.toLowerCase(),
                upperCase ? p.lastName.toUpperCase() : p.lastName.toLowerCase()));
    }

    @Bean
    CacheItemWriter<Person> personWriter() {
        return new CacheItemWriter<>();
    }

    public class CacheItemWriter<T> implements ItemWriter<T> {
        private List<T> items = synchronizedList(new LinkedList<>());

        @Override
        public void write(List<? extends T> items) {
            this.items.addAll(items);
        }

        public List<T> getItems() {
            return items;
        }

        public void clear() {
            items.clear();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Person {
        private String firstName;
        private String lastName;
    }
}
