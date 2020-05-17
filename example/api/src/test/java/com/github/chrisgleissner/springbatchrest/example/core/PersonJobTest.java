package com.github.chrisgleissner.springbatchrest.example.core;

import com.github.chrisgleissner.springbatchrest.api.core.jobexecution.JobExecution;
import com.github.chrisgleissner.springbatchrest.api.core.jobexecution.JobExecutionResource;
import com.github.chrisgleissner.springbatchrest.example.core.PersonJobConfig;
import com.github.chrisgleissner.springbatchrest.util.core.JobConfig;
import java.util.Optional;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = DEFINED_PORT)
@EnableAutoConfiguration
public class PersonJobTest {
    @LocalServerPort private int port;
    @Autowired private TestRestTemplate restTemplate;
    @Autowired private PersonJobConfig.CacheItemWriter<PersonJobConfig.Person> cacheItemWriter;

    @Test
    public void canStartJob() {
        cacheItemWriter.clear();
        startJob(Optional.empty(), Optional.empty());
        assertThat(cacheItemWriter.getItems()).hasSize(5);
        cacheItemWriter.getItems().forEach(p -> assertThat(p.getFirstName()).isEqualTo(p.getFirstName().toLowerCase()));

        cacheItemWriter.clear();
        startJob(Optional.of("D"), Optional.of(true));
        assertThat(cacheItemWriter.getItems()).hasSize(2);
        cacheItemWriter.getItems().forEach(p -> assertThat(p.getFirstName()).isEqualTo(p.getFirstName().toUpperCase()));

        cacheItemWriter.clear();
        startJob(Optional.of("To"), Optional.of(false));
        assertThat(cacheItemWriter.getItems()).hasSize(3);
        cacheItemWriter.getItems().forEach(p -> assertThat(p.getFirstName()).isEqualTo(p.getFirstName().toLowerCase()));
    }

    private JobExecution startJob(Optional<String> lastNamePrefix, Optional<Boolean> upperCase) {
        JobConfig.JobConfigBuilder jobConfigBuilder = JobConfig.builder()
                .name(PersonJobConfig.JOB_NAME).asynchronous(false);
        if (lastNamePrefix.isPresent())
            jobConfigBuilder.property(PersonJobConfig.LAST_NAME_PREFIX, lastNamePrefix.get());
        if (upperCase.isPresent())
            jobConfigBuilder.property("upperCase", "" + upperCase.get());
        JobConfig jobConfig = jobConfigBuilder.build();

        ResponseEntity<JobExecutionResource> responseEntity = restTemplate.postForEntity("http://localhost:" + port + "/jobExecutions",
                jobConfig, JobExecutionResource.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        return responseEntity.getBody().getJobExecution();
    }
}