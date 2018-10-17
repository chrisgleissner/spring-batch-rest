package com.github.chrisgleissner.springbatchrest.api;

import com.github.chrisgleissner.springbatchrest.util.adhoc.AdHocScheduler;
import com.github.chrisgleissner.springbatchrest.util.adhoc.AdHocBatchConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.batch.core.configuration.DuplicateJobException;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = DEFINED_PORT)
public class ServerTest {

    @TestConfiguration
    @Import(AdHocBatchConfig.class)
    public static class MyConfig {
    }

    private static final Logger logger = getLogger(ServerTest.class);
    private static final String CRON_EXPRESSION = "0/1 * * * * ?";
    private static final String JOB_NAME = "testJob";
    private static final int MAX_ITEMS = 100;

    private AtomicInteger readerSource = new AtomicInteger();
    private Set<String> writerTarget = new ConcurrentSkipListSet<>();
    private Semaphore allItemsWrittenSemaphore = new Semaphore(-MAX_ITEMS + 1);

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AdHocScheduler scheduler;

    private static AtomicBoolean firstExecution = new AtomicBoolean();

    @Before
    public void setUp() throws DuplicateJobException, InterruptedException {
        if (firstExecution.compareAndSet(false, true)) {
            scheduler.schedule(
                    JOB_NAME,
                    scheduler.jobs().get(JOB_NAME)
                            .incrementer(new RunIdIncrementer()) // adds unique parameter on each run so that job can be rerun
                            .flow(scheduler.steps().get("step")
                                    .<Integer, String>chunk(30)
                                    .reader(() -> {
                                        int i = readerSource.incrementAndGet();
                                        if (i % 10 == 0)
                                            logger.info("Read {} item(s) so far", i);
                                        return i <= MAX_ITEMS ? i : null;
                                    })
                                    .processor((ItemProcessor<Integer, String>) (i1) -> i1.toString())
                                    .writer(items -> {
                                        writerTarget.addAll(items);
                                        logger.info("Wrote {} item(s) so far", writerTarget.size());
                                        allItemsWrittenSemaphore.release(items.size());
                                    })
                                    .allowStartIfComplete(true)
                                    .build()).end().build(),
                    CRON_EXPRESSION);
            scheduler.start();

            allItemsWrittenSemaphore.tryAcquire(1, 3, SECONDS);
            assertThat(writerTarget).hasSize(MAX_ITEMS);
        }
    }

    @Test
    public void jobExecution() {
        assertThat(this.restTemplate.getForObject(url("/jobExecution?exitStatus=COMPLETED"), String.class))
                .contains("\"status\":\"COMPLETED\"").contains("\"id\":0,\"jobId\":0");
        assertThat(this.restTemplate.getForObject(url("/jobExecution?exitStatus=FAILED"), String.class))
                .contains("jobExecution?exitStatus=exitCode%3DFAILED");
    }

    @Test
    public void jobDetail() {
        assertThat(this.restTemplate.getForObject(url("/jobDetail"), String.class))
                .contains(CRON_EXPRESSION);
        assertThat(this.restTemplate.getForObject(url("/jobDetail?enabled=false"), String.class))
                .doesNotContain(CRON_EXPRESSION);
        assertThat(this.restTemplate.getForObject(url("/jobDetail?springBatchJobName=" + JOB_NAME), String.class))
                .contains(CRON_EXPRESSION).contains(JOB_NAME);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}