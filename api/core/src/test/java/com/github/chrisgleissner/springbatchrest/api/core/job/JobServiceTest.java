package com.github.chrisgleissner.springbatchrest.api.core.job;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.configuration.JobRegistry;

import java.util.Collection;

import static com.github.chrisgleissner.springbatchrest.api.core.Fixtures.configureMock;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class JobServiceTest {

    @Mock
    private JobRegistry jobRegistry;

    private JobService jobService;

    @Before
    public void setUp() {
        configureMock(jobRegistry);
        jobService = new JobService(jobRegistry);
    }

    @Test
    public void jobs() {
        Collection<Job> jobs = jobService.jobs();
        assertThat(jobs).hasSize(2);

    }
}