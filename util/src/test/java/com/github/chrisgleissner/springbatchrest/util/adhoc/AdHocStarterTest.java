package com.github.chrisgleissner.springbatchrest.util.adhoc;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AdHocBatchConfig.class)
public class AdHocStarterTest {

    @Autowired
    private AdHocStarter starter;

    @Test
    public void startWorks() throws InterruptedException {
        int numberOfIterations = 2;
        int numberOfJobsPerIteration = 2;
        for (int i = 0; i < numberOfIterations; i++) {
            CountDownLatch latch = new CountDownLatch(numberOfJobsPerIteration);
            for (int j = 0; j < numberOfJobsPerIteration; j++) {
                String jobName = "AdHocStarterTest" + j;
                starter.start(jobName, () -> {
                    log.info("Running " + jobName);
                    latch.countDown();
                }, new JobParameters());
            }
            assertThat(latch.await(3, SECONDS)).isTrue();
        }
    }
}
