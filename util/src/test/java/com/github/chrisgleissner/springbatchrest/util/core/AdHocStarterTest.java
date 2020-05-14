package com.github.chrisgleissner.springbatchrest.util.core;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.chrisgleissner.springbatchrest.util.core.config.AdHocBatchConfig;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static com.github.chrisgleissner.springbatchrest.util.core.property.JobPropertyResolvers.JobProperties;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = AdHocBatchConfig.class)
@TestPropertySource(properties = "foo=bar")
public class AdHocStarterTest {

    @Autowired
    private AdHocStarter starter;

    @Autowired
    private JobBuilder jobBuilder;
    
    private static final int numberOfIterations = 2;
    private static final int numberOfJobsPerIteration = 2;
    
    @Test
    public void startWorks() throws InterruptedException {
        // Check that asynchronous execution with property overrides works
        Set<String> propertyValues = new HashSet<>();
        int propertyValue = 0;
        for (int i = 0; i < numberOfIterations; i++) {
            CountDownLatch latch = new CountDownLatch(numberOfJobsPerIteration);
            for (int j = 0; j < numberOfJobsPerIteration; j++) {
                String jobName = "AdHocStarterTest" + j;
                Job jobToRun = jobBuilder.createJob(jobName, () -> {
                    log.info("Running " + jobName);
                    propertyValues.add(JobProperties.of(jobName).getProperty("foo"));
                    latch.countDown();
                });
                HashMap<String, Object> propMap = new HashMap<String, Object>();
                propMap.put("foo", propertyValue++);
                starter.start(jobToRun, true, propMap);
            }
            assertThat(latch.await(2, SECONDS)).isTrue();
            assertThat(propertyValues).hasSize(propertyValue); // TODO: Intermittent failures here - presumed timing issue
        }
        Thread.sleep(100); // Job completion takes place after latch is counted down
        assertThat(JobProperties.of("AdHocStarterTest0").getProperty("foo")).isEqualTo("bar");

        // Check that synchronous execution without overrides works
        starter.start(JobConfig.builder()
                .name("AdHocStarterTest0")
                .asynchronous(false)
                .build());
        assertThat(propertyValues).contains("bar");
    }

    @Test
    public void startJobConfigWorks() throws InterruptedException {
        // Check that asynchronous execution with property overrides works
        Set<String> propertyValues = new HashSet<>();
        int propertyValue = 0;
        for (int i = 0; i < numberOfIterations; i++) {
            CountDownLatch latch = new CountDownLatch(numberOfJobsPerIteration);
            for (int j = 0; j < numberOfJobsPerIteration; j++) {
                String jobName = "AdHocStarterTest" + j;
                jobBuilder.createJob(jobName, () -> {
                    log.info("Running " + jobName);
                    propertyValues.add(JobProperties.of(jobName).getProperty("foo"));
                    latch.countDown();
                });
                starter.start(JobConfig.builder()
                        .name(jobName)
                        .property("foo", "" + propertyValue++)
                        .asynchronous(true).build());
            }
            assertThat(latch.await(2, SECONDS)).isTrue();
            assertThat(propertyValues).hasSize(propertyValue);
        }
        Thread.sleep(100); // Job completion takes place after latch is counted down
        assertThat(JobProperties.of("AdHocStarterTest0").getProperty("foo")).isEqualTo("bar");

        // Check that synchronous execution without overrides works
        starter.start(JobConfig.builder()
                .name("AdHocStarterTest0")
                .asynchronous(false)
                .build());
        assertThat(propertyValues).contains("bar");
    }
}
