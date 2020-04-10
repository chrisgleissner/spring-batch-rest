package com.github.chrisgleissner.springbatchrest.api.core.jobexecution;

import com.github.chrisgleissner.springbatchrest.api.core.jobexecution.provider.AllJobExecutionProvider;
import com.github.chrisgleissner.springbatchrest.api.core.jobexecution.provider.CachedJobExecutionProvider;
import com.github.chrisgleissner.springbatchrest.util.core.AdHocStarter;
import com.github.chrisgleissner.springbatchrest.util.core.JobConfig;
import com.github.chrisgleissner.springbatchrest.util.core.property.JobExecutionAspect;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.explore.JobExplorer;

import javax.batch.operations.NoSuchJobExecutionException;
import java.util.Collection;
import java.util.Optional;

import static com.github.chrisgleissner.springbatchrest.api.core.Fixtures.*;
import static java.lang.Integer.MAX_VALUE;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
        JobExecution jobExecution = jobExecutionService.launch(JobConfig.builder().name("j1").build());
        assertThat(jobExecution.getJobName()).matches("j1");
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.STARTED);
    }

    @Test
    public void jobExecutionsAll() {
        Collection<JobExecution> jes =
                jobExecutionService.jobExecutions(empty(), empty(), MAX_VALUE);
        Assertions.assertThat(jes).hasSize(6);
    }

    @Test
    public void jobExecutionsId() {
        JobExecution je = jobExecutionService.jobExecution(je11.getId());
        assertThat(je).isNotNull();
    }

    @Test(expected = NoSuchJobExecutionException.class)
    public void jobExecutionsIdNotFound() {
        jobExecutionService.jobExecution(10);
    }

    @Test(expected = NoSuchJobExecutionException.class)
    public void jobExecutionsIdNotFoundNegativeId() {
        jobExecutionService.jobExecution(-1);
    }

    @Test
    public void jobExecutionsJobNameRegexp() {
        Collection<JobExecution> jes =
                jobExecutionService.jobExecutions(Optional.of("j1"), empty(), MAX_VALUE);
        Assertions.assertThat(jes).hasSize(2);
    }

    @Test
    public void jobExecutionsStatus() {
        Collection<JobExecution> jes =
                jobExecutionService.jobExecutions(Optional.of("j1"), Optional.of(ExitStatus.COMPLETED.getExitCode()), MAX_VALUE);
        Assertions.assertThat(jes).hasSize(1);
    }

    @Test
    public void jobExecutionsMaxNumberOfJobInstancesFailed() {
        Collection<JobExecution> jes =
                jobExecutionService.jobExecutions(empty(), Optional.of(ExitStatus.FAILED.getExitCode()), 1);
        Assertions.assertThat(jes).hasSize(2);
        Assertions.assertThat(jes).extracting(je -> je.getExitCode()).allMatch(s -> s.equals("FAILED"));
    }

    @Test
    public void jobExecutionsMaxNumberOfJobInstancesCompleted() {
        Collection<JobExecution> jes =
                jobExecutionService.jobExecutions(empty(), Optional.of(ExitStatus.COMPLETED.getExitCode()), 1);
        Assertions.assertThat(jes).hasSize(2);
        Assertions.assertThat(jes).extracting(je -> je.getJobName()).containsExactly(JOB_NAME_2, JOB_NAME_1);
    }
}