package com.github.chrisgleissner.springbatchrest.test;

import com.github.chrisgleissner.springbatchrest.api.jobexecution.JobExecution;
import com.github.chrisgleissner.springbatchrest.api.jobexecution.JobExecutionResource;
import com.github.chrisgleissner.springbatchrest.test.PersonJobConfig.CacheItemWriter;
import com.github.chrisgleissner.springbatchrest.test.PersonJobConfig.Person;
import com.github.chrisgleissner.springbatchrest.util.adhoc.JobConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.beans.factory.annotation.Autowired;
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
public class PersonJobTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CacheItemWriter<Person> cacheItemWriter;

    @Autowired
    private JobRegistry jobRegistry;

    @Test
    public void canStartJob() throws NoSuchJobException {
        Job job = jobRegistry.getJob(PersonJobConfig.JOB_NAME);
        assertThat(job).isNotNull();

        cacheItemWriter.clear();
        startJob("D", true);
        assertThat(cacheItemWriter.getItems()).hasSize(2);
        cacheItemWriter.getItems().forEach(p -> assertThat(p.getFirstName()).isEqualTo(p.getFirstName().toUpperCase()));

        cacheItemWriter.clear();
        startJob("To", false);
        assertThat(cacheItemWriter.getItems()).hasSize(3);
        cacheItemWriter.getItems().forEach(p -> assertThat(p.getFirstName()).isEqualTo(p.getFirstName().toLowerCase()));
    }

    private JobExecution startJob(String lastNamePrefix, boolean upperCase) {
        ResponseEntity<JobExecutionResource> responseEntity = restTemplate.postForEntity("http://localhost:" + port + "/jobExecutions",
                JobConfig.builder()
                        .name(PersonJobConfig.JOB_NAME)
                        .property(PersonJobConfig.LAST_NAME_PREFIX, lastNamePrefix)
                        .property("upperCase", "" + upperCase)
                        .asynchronous(false).build(),
                JobExecutionResource.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        return responseEntity.getBody().getJobExecution();
    }


}