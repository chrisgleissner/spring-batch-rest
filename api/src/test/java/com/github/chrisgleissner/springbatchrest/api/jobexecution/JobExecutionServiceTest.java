package com.github.chrisgleissner.springbatchrest.api.jobexecution;

import com.github.chrisgleissner.springbatchrest.util.adhoc.AdHocStarter;
import com.github.chrisgleissner.springbatchrest.util.adhoc.JobConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.NoSuchJobException;

import java.util.Collection;
import java.util.Optional;

import static com.github.chrisgleissner.springbatchrest.api.MockSetup.configureMock;
import static com.github.chrisgleissner.springbatchrest.api.MockSetup.configureMockForJobExecutionsService;
import static java.lang.Integer.MAX_VALUE;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class JobExecutionServiceTest {

    @Mock
    private JobExplorer jobExplorer;
    @Mock
    private AdHocStarter adHocStarter;
    private JobExecutionService jobExecutionService;

    @Before
    public void setUp() throws NoSuchJobException {
        configureMock(jobExplorer);
        configureMock(adHocStarter);
        configureMockForJobExecutionsService(jobExplorer);
        jobExecutionService = new JobExecutionService(jobExplorer, adHocStarter);
    }

    @Test
    public void launchJob() {
        com.github.chrisgleissner.springbatchrest.api.jobexecution.JobExecution jobExecution = jobExecutionService.launch(JobConfig.builder().name("j1").build());
        assertThat(jobExecution.getJobName()).matches("j1");
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }

    @Test
    public void jobExecutionsAll() {
        Collection<JobExecution> jes =
                jobExecutionService.jobExecutions(empty(), empty(), MAX_VALUE, MAX_VALUE);
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
                jobExecutionService.jobExecutions(Optional.of("j1"), empty(), MAX_VALUE, MAX_VALUE);
        assertThat(jes).hasSize(3);
    }

    @Test
    public void jobExecutionsStatus() {
        Collection<JobExecution> jes =
                jobExecutionService.jobExecutions(Optional.of("j1"), Optional.of(ExitStatus.COMPLETED.getExitCode()), MAX_VALUE, MAX_VALUE);
        assertThat(jes).hasSize(2);
    }

    @Test
    public void jobExecutionsMaxNumberOfJobInstances() {
        Collection<JobExecution> jes =
                jobExecutionService.jobExecutions(empty(), Optional.of(ExitStatus.FAILED.getExitCode()), 1, MAX_VALUE);
        assertThat(jes).hasSize(3);
    }

    @Test
    public void jobExecutionsMaxNumberOfJobExecutionsPerInstance() {
        Collection<JobExecution> jes =
                jobExecutionService.jobExecutions(empty(), Optional.of(ExitStatus.COMPLETED.getExitCode()), MAX_VALUE, 1);
        assertThat(jes).hasSize(3);
    }
}