package com.github.chrisgleissner.springbatchrest.util.core;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.chrisgleissner.springbatchrest.util.core.config.AdHocBatchConfig;

import java.util.HashMap;
import java.util.Optional;
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
    private static final String JOB_NAME = "AdHocStarterTest";
    private static final int NUMBER_OF_ITERATIONS = 3;
    private static final int NUMBER_OF_JOBS_PER_ITERATION = 2;
    public static final String PROPERTY_NAME = "foo";

    @Autowired Environment env;
    @Autowired private AdHocStarter starter;
    @Autowired private JobBuilder jobBuilder;

    @Test
    public void startAsynchPropertyResolverConsumerJobWithPropertyMap() throws InterruptedException {
        assertPropertyResolution((propertyValue, readPropertyValues, latch)
                -> starter.start(createJobFromPropertyResolverConsumer(readPropertyValues, latch), true, propertyMap(propertyValue)));
    }

    @Test
    public void startAsynchPropertyResolverConsumerJobWithJobConfig() throws InterruptedException {
        assertPropertyResolution((propertyValue, readPropertyValues, latch) -> {
            createJobFromPropertyResolverConsumer(readPropertyValues, latch);
            starter.start(JobConfig.builder()
                    .name(JOB_NAME)
                    .property(PROPERTY_NAME, "" + propertyValue)
                    .asynchronous(true).build());
        });
    }

    @Test
    public void startSynchRunnable() throws InterruptedException {
        assertPropertyResolution((propertyValue, readPropertyValues, latch)
                -> starter.start(createJobFromRunnable(readPropertyValues, latch), false, propertyMap(propertyValue)));
    }

    @Test
    public void startAsynchStepExecutionConsumer() throws InterruptedException {
        assertPropertyResolution((propertyValue, readPropertyValues, latch)
                -> starter.start(createJobFromStepExecutionConsumer(readPropertyValues, latch), true, propertyMap(propertyValue)));
    }

    private static HashMap<String, Object> propertyMap(int propertyValue) {
        final HashMap<String, Object> propMap = new HashMap<>();
        propMap.put(PROPERTY_NAME, propertyValue);
        return propMap;
    }

    private Job createJobFromRunnable(Set<String> readPropertyValues, CountDownLatch latch) {
        return jobBuilder.createJob(JOB_NAME, () -> {
            readPropertyValues.add(JobProperties.of(JOB_NAME).getProperty(PROPERTY_NAME));
            latch.countDown();
        });
    }

    private Job createJobFromPropertyResolverConsumer(Set<String> readPropertyValues, CountDownLatch latch) {
        return jobBuilder.createJob(JOB_NAME, (propertyResolver) -> {
            readPropertyValues.add(propertyResolver.getProperty(PROPERTY_NAME));
            latch.countDown();
        });
    }

    private Job createJobFromStepExecutionConsumer(Set<String> readPropertyValues, CountDownLatch latch) {
        return jobBuilder.createJobFromStepExecutionConsumer(JOB_NAME, (stepExecution) -> {
            String propertyValue = Optional.ofNullable(stepExecution.getJobParameters().getString(PROPERTY_NAME))
                    .orElseGet(() -> env.getProperty(PROPERTY_NAME));
            readPropertyValues.add(propertyValue);
            latch.countDown();
        });
    }

    private void assertPropertyResolution(JobStarter jobStarter) throws InterruptedException {
        // Check that asynchronous execution with property overrides works
        final Set<String> readPropertyValues = new ConcurrentSkipListSet<>();
        int propertyValue = 0;
        for (int i = 0; i < NUMBER_OF_ITERATIONS; i++) {
            final CountDownLatch latch = new CountDownLatch(NUMBER_OF_JOBS_PER_ITERATION);
            for (int j = 0; j < NUMBER_OF_JOBS_PER_ITERATION; j++) {
                jobStarter.startJob(propertyValue++, readPropertyValues, latch);
            }
            assertThat(latch.await(3, SECONDS)).isTrue();
            assertThat(readPropertyValues).hasSize(propertyValue);
        }
        Thread.sleep(100); // Job completion takes place after latch is counted down
        assertThat(JobProperties.of(JOB_NAME).getProperty(PROPERTY_NAME)).isEqualTo("bar");

        // Check that synchronous execution without overrides works
        starter.start(JobConfig.builder()
                .name(JOB_NAME)
                .asynchronous(false)
                .build());
        assertThat(readPropertyValues).contains("bar");
    }

    private interface JobStarter {
        void startJob(int propertyValue, Set<String> propertyValues, CountDownLatch latch);
    }
}
