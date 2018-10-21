package com.github.chrisgleissner.springbatchrest.api.jobexecution;

import com.github.chrisgleissner.springbatchrest.util.adhoc.AdHocStarter;
import com.github.chrisgleissner.springbatchrest.util.adhoc.JobConfig;
import com.github.chrisgleissner.springbatchrest.util.adhoc.observer.JobExecutionAspect;
import com.github.chrisgleissner.springbatchrest.util.adhoc.property.JobPropertyResolver;
import com.github.chrisgleissner.springbatchrest.util.adhoc.property.JobPropertyResolvers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;

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