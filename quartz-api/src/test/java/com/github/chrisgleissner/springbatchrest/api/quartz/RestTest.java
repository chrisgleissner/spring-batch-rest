package com.github.chrisgleissner.springbatchrest.api.quartz;

import com.github.chrisgleissner.springbatchrest.api.core.jobexecution.JobExecution;
import com.github.chrisgleissner.springbatchrest.api.core.jobexecution.JobExecutionResource;
import com.github.chrisgleissner.springbatchrest.util.core.JobBuilder;
import com.github.chrisgleissner.springbatchrest.util.core.JobConfig;
import com.github.chrisgleissner.springbatchrest.util.core.config.AdHocBatchConfig;
import com.github.chrisgleissner.springbatchrest.util.quartz.AdHocScheduler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.batch.core.ExitStatus.COMPLETED;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = DEFINED_PORT)
@TestPropertySource(properties = "ServerTest-property=0")
@Import(AdHocBatchConfig.class)
public class RestTest {
    private static final String JOB_NAME = "ServerTest-job";
    private static final String PROPERTY_NAME = "ServerTest-property";
    private static final String EXCEPTION_MESSAGE_PROPERTY_NAME = "ServerTest-exceptionMessage";
    private static final String CRON_EXPRESSION = "0/1 * * * * ?";
    private static final Set<String> propertyValues = new ConcurrentSkipListSet<>();

    private final JobConfig jobConfig = JobConfig.builder().name(JOB_NAME).asynchronous(false).build();
    private final CountDownLatch jobExecutedOnce = new CountDownLatch(1);
    private final AtomicBoolean firstExecution = new AtomicBoolean(true);

    @LocalServerPort private int port;
    @Autowired private TestRestTemplate restTemplate;
    @Autowired private AdHocScheduler adHocScheduler;
    @Autowired private JobBuilder jobBuilder;

    @Before
    public void setUp() throws InterruptedException {
        if (firstExecution.compareAndSet(true, false)) {
            Job job = jobBuilder.createJob(JOB_NAME, propertyResolver -> {
                String propertyValue = propertyResolver.getProperty(PROPERTY_NAME);
                propertyValues.add(propertyValue);

                String exceptionMessage = propertyResolver.getProperty(EXCEPTION_MESSAGE_PROPERTY_NAME);
                if (exceptionMessage != null)
                    throw new RuntimeException(exceptionMessage);

                jobExecutedOnce.countDown();
            });
            adHocScheduler.schedule(JOB_NAME, job, CRON_EXPRESSION);
            adHocScheduler.start();
            jobExecutedOnce.await(2, SECONDS);
            adHocScheduler.pause();
        }
    }

    @Test
    public void jobsCanBeStartedWithDifferentProperties() {
        assertThat(propertyValues).containsExactly("0");

        JobExecution je1 = startJob("1");
        assertThat(propertyValues).containsExactly("0", "1");

        JobExecution je2 = startJob("2");
        assertThat(propertyValues).containsExactly("0", "1", "2");

        assertThat(je1.getExitCode()).isEqualTo(COMPLETED.getExitCode());
        assertThat(je2.getExitCode()).isEqualTo(COMPLETED.getExitCode());

        assertThat(restTemplate.getForObject(url("/jobExecutions?exitCode=COMPLETED"), String.class))
                .contains("\"status\":\"COMPLETED\"").contains("\"jobName\":\"ServerTest-job\"");
    }

    @Test
    public void jobExceptionMessageIsPropagatedToClient() {
        String exceptionMessage = "excepted exception";
        JobExecution je = startJobThatThrowsException(exceptionMessage);
        assertThat(je.getExitCode()).isEqualTo(ExitStatus.FAILED.getExitCode());
        assertThat(je.getExitDescription()).contains(exceptionMessage);

        assertThat(restTemplate.getForObject(url("/jobExecutions?exitCode=FAILED"), String.class))
                .contains("\"exitCode\":\"FAILED\",\"exitDescription\":\"java.lang.RuntimeException");
    }

    @Test
    public void jobDetails() {
        assertThat(restTemplate.getForObject(url("/jobDetails?springBatchJobName=" + JOB_NAME), String.class))
                .contains(JOB_NAME);
    }

    @Test
    public void swagger() {
        assertThat(restTemplate.getForObject(url("v3/api-docs"), String.class))
                .contains("\"openapi\":\"3.0.1\"");
    }

    private JobExecution startJob(String propertyValue) {
        ResponseEntity<JobExecutionResource> responseEntity = restTemplate.postForEntity(url("/jobExecutions"),
                jobConfig.toBuilder().property(PROPERTY_NAME, propertyValue).build(),
                JobExecutionResource.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        return responseEntity.getBody().getJobExecution();
    }

    private JobExecution startJobThatThrowsException(String exceptionMessage) {
        ResponseEntity<JobExecutionResource> responseEntity = restTemplate.postForEntity(url("/jobExecutions"),
                jobConfig.toBuilder().property(EXCEPTION_MESSAGE_PROPERTY_NAME, exceptionMessage).build(),
                JobExecutionResource.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        return responseEntity.getBody().getJobExecution();
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}