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
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;

import static com.github.chrisgleissner.springbatchrest.util.core.property.JobPropertyResolvers.JobProperties;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = AdHocBatchConfig.class)
@TestPropertySource(properties = "foo=bar")
@Slf4j
public class AdHocStarterTest {
    private static final int NUMBER_OF_ITERATIONS = 3;
    private static final int NUMBER_OF_JOBS_PER_ITERATION = 2;

    @Autowired private AdHocStarter starter;
    @Autowired private JobBuilder jobBuilder;

    @Test
    public void startJobWithPropertyMap() throws InterruptedException {
        assertThatConcurrentlyStartedJobsCanHaveDifferentProperties((jobName, propertyValue, propertyValues, latch) -> {
            final Job jobToRun = jobBuilder.createJob(jobName, () -> {
                log.info("Running " + jobName);
                propertyValues.add(JobProperties.of(jobName).getProperty("foo"));
                latch.countDown();
            });
            final HashMap<String, Object> propMap = new HashMap<String, Object>();
            propMap.put("foo", propertyValue);
            starter.start(jobToRun, true, propMap);
        });
    }

    @Test
    public void startJobWithJobConfig() throws InterruptedException {
        assertThatConcurrentlyStartedJobsCanHaveDifferentProperties((jobName, propertyValue, propertyValues, latch) -> {
            jobBuilder.createJob(jobName, () -> {
                log.info("Running " + jobName);
                propertyValues.add(JobProperties.of(jobName).getProperty("foo"));
                latch.countDown();
            });
            starter.start(JobConfig.builder()
                    .name(jobName)
                    .property("foo", "" + propertyValue)
                    .asynchronous(true).build());
        });
    }

    private void assertThatConcurrentlyStartedJobsCanHaveDifferentProperties(JobStarter jobStarter) throws InterruptedException {
        // Check that asynchronous execution with property overrides works
        final Set<String> propertyValues = new ConcurrentSkipListSet<>();
        int propertyValue = 0;
        for (int i = 0; i < NUMBER_OF_ITERATIONS; i++) {
            final CountDownLatch latch = new CountDownLatch(NUMBER_OF_JOBS_PER_ITERATION);
            for (int j = 0; j < NUMBER_OF_JOBS_PER_ITERATION; j++) {
                final String jobName = String.format("AdHocStarterTest-%s-%s", i, j);
                jobStarter.startJob(jobName, propertyValue++, propertyValues, latch);
            }
            assertThat(latch.await(3, SECONDS)).isTrue();
            assertThat(propertyValues).hasSize(propertyValue);
        }
        Thread.sleep(100); // Job completion takes place after latch is counted down
        assertThat(JobProperties.of("AdHocStarterTest-0-0").getProperty("foo")).isEqualTo("bar");

        // Check that synchronous execution without overrides works
        starter.start(JobConfig.builder()
                .name("AdHocStarterTest-0-0")
                .asynchronous(false)
                .build());
        assertThat(propertyValues).contains("bar");
    }

    private interface JobStarter {
        void startJob(String jobName, int propertyValue, Set<String> propertyValues, CountDownLatch latch);
    }
}
