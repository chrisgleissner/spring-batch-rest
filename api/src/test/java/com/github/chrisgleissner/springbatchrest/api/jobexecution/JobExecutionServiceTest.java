package com.github.chrisgleissner.springbatchrest.api.jobexecution;

import com.github.chrisgleissner.springbatchrest.api.jobexecution.provider.AllJobExecutionProvider;
import com.github.chrisgleissner.springbatchrest.api.jobexecution.provider.CachedJobExecutionProvider;
import com.github.chrisgleissner.springbatchrest.util.adhoc.AdHocStarter;
import com.github.chrisgleissner.springbatchrest.util.adhoc.JobConfig;
import com.github.chrisgleissner.springbatchrest.util.adhoc.property.JobExecutionAspect;
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

import static com.github.chrisgleissner.springbatchrest.api.Fixtures.*;
import static java.lang.Integer.MAX_VALUE;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JobExecutionServiceTest {

    @Mock
    private JobExplorer jobExplorer;
    @Mock
    private AdHocStarter adHocStarter;
    @Mock
    private JobExecutionAspect jobExecutionAspect;
    private AllJobExecutionProvider allJobExecutionProvider;
    private CachedJobExecutionProvider cachedJobExecutionProvider;
    private JobExecutionService jobExecutionService;

    @Before
    public void setUp() {
        configureMock(jobExplorer);
        allJobExecutionProvider = new AllJobExecutionProvider(jobExplorer);
        cachedJobExecutionProvider = new CachedJobExecutionProvider(jobExecutionAspect, allJobExecutionProvider, 3);
        configureMock(adHocStarter);

        configureForJobExecutionsService(jobExplorer);
        when(jobExplorer.getJobExecution(je11.getId())).thenReturn(je11);

        configureForJobExecutionsService(cachedJobExecutionProvider);
        jobExecutionService = new JobExecutionService(jobExplorer, cachedJobExecutionProvider, adHocStarter);
    }

    @Test
    public void launchJob() {
        com.github.chrisgleissner.springbatchrest.api.jobexecution.JobExecution jobExecution = jobExecutionService.launch(JobConfig.builder().name("j1").build());
        assertThat(jobExecution.getJobName()).matches("j1");
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.STARTED);
    }

    @Test
    public void jobExecutionsAll() {
        Collection<JobExecution> jes =
                jobExecutionService.jobExecutions(empty(), empty(), MAX_VALUE);
        assertThat(jes).hasSize(6);
    }

    @Test
    public void jobExecutionsId() {
        JobExecution je = jobExecutionService.jobExecution(je11.getId());
        assertThat(je).isNotNull();
    }

    @Test
    public void jobExecutionsJobNameRegexp() {
        Collection<JobExecution> jes =
                jobExecutionService.jobExecutions(Optional.of("j1"), empty(), MAX_VALUE);
        assertThat(jes).hasSize(2);
    }

    @Test
    public void jobExecutionsStatus() {
        Collection<JobExecution> jes =
                jobExecutionService.jobExecutions(Optional.of("j1"), Optional.of(ExitStatus.COMPLETED.getExitCode()), MAX_VALUE);
        assertThat(jes).hasSize(1);
    }

    @Test
    public void jobExecutionsMaxNumberOfJobInstancesFailed() {
        Collection<JobExecution> jes =
                jobExecutionService.jobExecutions(empty(), Optional.of(ExitStatus.FAILED.getExitCode()), 1);
        assertThat(jes).hasSize(2);
        assertThat(jes).extracting(je -> je.getExitCode()).allMatch(s -> s.equals("FAILED"));
    }

    @Test
    public void jobExecutionsMaxNumberOfJobInstancesCompleted() {
        Collection<JobExecution> jes =
                jobExecutionService.jobExecutions(empty(), Optional.of(ExitStatus.COMPLETED.getExitCode()), 1);
        assertThat(jes).hasSize(2);
        assertThat(jes).extracting(je -> je.getJobName()).containsExactly(JOB_NAME_1, JOB_NAME_2);
    }
}