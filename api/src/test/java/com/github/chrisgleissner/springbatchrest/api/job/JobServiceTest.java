package com.github.chrisgleissner.springbatchrest.api.job;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static com.github.chrisgleissner.springbatchrest.api.MockSetup.configureMock;

@RunWith(MockitoJUnitRunner.class)
public class JobServiceTest {

    @Mock
    private JobExplorer jobExplorer;

    private JobService jobService;

    @Before
    public void setUp() {
        configureMock(jobExplorer);
        jobService = new JobService(jobExplorer);
    }

    @Test
    public void jobs() {
        Collection<com.github.chrisgleissner.springbatchrest.api.job.Job> jobs = jobService.jobs();
        assertThat(jobs).hasSize(2);

    }
}