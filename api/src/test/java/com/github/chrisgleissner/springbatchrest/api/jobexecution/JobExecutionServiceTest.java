package com.github.chrisgleissner.springbatchrest.api.jobexecution;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.NoSuchJobException;

import java.util.Collection;
import java.util.Optional;

import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static com.github.chrisgleissner.springbatchrest.api.MockSetup.configureMock;
import static com.github.chrisgleissner.springbatchrest.api.MockSetup.configureMockForJobExecutionsService;

@RunWith(MockitoJUnitRunner.class)
public class JobExecutionServiceTest {

    @Mock
    private JobExplorer jobExplorer;

    private JobExecutionService jobExecutionService;

    @Before
    public void setUp() throws NoSuchJobException {
        configureMock(jobExplorer);
        configureMockForJobExecutionsService(jobExplorer);
        jobExecutionService = new JobExecutionService(jobExplorer);
    }

    @Test
    public void jobExecutionsAll() {
        Collection<JobExecution> jes =
                jobExecutionService.jobExecutions(empty(), empty(), empty(), empty());
        assertThat(jes).hasSize(6);
    }

    @Test
    public void jobExecutionsId() {
        JobExecution je =
                jobExecutionService.jobExecution(1L);
        assertThat(je).isNotNull();
    }

    @Test
    public void jobExecutionsJobNameRegexp() {
        Collection<JobExecution> jes =
                jobExecutionService.jobExecutions(Optional.of("j1"), empty(), empty(), empty());
        assertThat(jes).hasSize(3);
    }

    @Test
    public void jobExecutionsStatus() {
        Collection<JobExecution> jes =
                jobExecutionService.jobExecutions(Optional.of("j1"), Optional.of(ExitStatus.COMPLETED), empty(), empty());
        assertThat(jes).hasSize(2);
    }

    @Test
    public void jobExecutionsMaxNumberOfJobInstances() {
        Collection<JobExecution> jes =
                jobExecutionService.jobExecutions(empty(), Optional.of(ExitStatus.FAILED), Optional.of(1), empty());
        assertThat(jes).hasSize(3);
    }

    @Test
    public void jobExecutionsMaxNumberOfJobExecutionsPerInstance() {
        Collection<JobExecution> jes =
                jobExecutionService.jobExecutions(empty(), Optional.of(ExitStatus.COMPLETED), empty(), Optional.of(1));
        assertThat(jes).hasSize(3);
    }
}