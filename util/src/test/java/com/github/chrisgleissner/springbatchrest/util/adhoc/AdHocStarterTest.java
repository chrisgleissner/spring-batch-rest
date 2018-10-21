package com.github.chrisgleissner.springbatchrest.util.adhoc;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static com.github.chrisgleissner.springbatchrest.util.adhoc.property.JobPropertyResolvers.JobProperties;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AdHocBatchConfig.class)
@TestPropertySource(properties = "foo=bar" )
public class AdHocStarterTest {

    @Autowired
    private AdHocStarter starter;

    @Autowired
    private JobBuilder jobBuilder;

    @Test
    public void startWorks() throws InterruptedException {
        // Check that asynchronous execution with property overrides works
        int numberOfIterations = 2;
        int numberOfJobsPerIteration = 2;
        int propertyValue = 0;
        Set<String> propertyValues = new HashSet<>();
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
