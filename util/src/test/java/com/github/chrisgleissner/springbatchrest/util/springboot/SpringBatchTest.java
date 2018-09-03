package com.github.chrisgleissner.springbatchrest.util.springboot;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import com.github.chrisgleissner.springbatchrest.util.CacheItemWriter;
import com.github.chrisgleissner.springbatchrest.util.JobCompletionNotificationListener;
import com.github.chrisgleissner.springbatchrest.util.Person;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests the idiomatic Quartz scheduling of Spring Batch jobs via Spring Boot.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringBatchTest {

    @Autowired
    private CacheItemWriter<Person> writer;

    @Autowired
    private JobCompletionNotificationListener completionNotificationListener;

    @Test
    public void quartzSchedulerWorks() throws InterruptedException {
        completionNotificationListener.awaitCompletionOfJobs(2, 5_000);
        assertThat(writer.getItems().size(), is(10));
        assertThat(writer.getItems().iterator().next().getFirstName(), is("JILL"));
    }
}
