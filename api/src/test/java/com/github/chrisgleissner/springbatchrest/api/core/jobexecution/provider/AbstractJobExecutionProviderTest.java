package com.github.chrisgleissner.springbatchrest.api.core.jobexecution.provider;

import com.github.chrisgleissner.springbatchrest.api.core.Fixtures;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.JobExecution;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.batch.core.ExitStatus.COMPLETED;
import static org.springframework.batch.core.ExitStatus.FAILED;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractJobExecutionProviderTest {

    public static final int MAX_NUMBER_OF_EXECUTIONS_PER_JOB_NAME = 5;

    protected abstract JobExecutionProvider provider();

    @Test
    public void worksForEmptyOptionals() {
        Collection<JobExecution> jes = provider().getJobExecutions(Optional.empty(), Optional.empty(), MAX_NUMBER_OF_EXECUTIONS_PER_JOB_NAME);
        assertThat(jes).containsExactly(Fixtures.je24, Fixtures.je23, Fixtures.je22, Fixtures.je21, Fixtures.je12, Fixtures.je11);
    }

    @Test
    public void worksForCompleted() {
        Collection<JobExecution> jes = provider().getJobExecutions(Optional.of(Fixtures.JOB_NAME_2), Optional.of(COMPLETED.getExitCode()), MAX_NUMBER_OF_EXECUTIONS_PER_JOB_NAME);
        assertThat(jes).containsExactly(Fixtures.je22, Fixtures.je21);
    }

    @Test
    public void limitsReturnedValuesForCompleted() {
        Collection<JobExecution> jes = provider().getJobExecutions(Optional.of(Fixtures.JOB_NAME_2), Optional.of(COMPLETED.getExitCode()), 1);
        assertThat(jes).containsExactly(Fixtures.je22);
    }

    @Test
    public void worksForFailed() {
        Collection<JobExecution> jes = provider().getJobExecutions(Optional.of(Fixtures.JOB_NAME_1), Optional.of(FAILED.getExitCode()), MAX_NUMBER_OF_EXECUTIONS_PER_JOB_NAME);
        assertThat(jes).containsExactly(Fixtures.je12);
    }

    @Test
    public void limitsReturnedValuesForFailed() {
        Collection<JobExecution> jes = provider().getJobExecutions(Optional.of(Fixtures.JOB_NAME_2), Optional.of(FAILED.getExitCode()), 1);
        assertThat(jes).containsExactly(Fixtures.je24);
    }

    @Test
    public void sortsResultsInDescendingDateOrder() {
        Collection<JobExecution> jes = provider().getJobExecutions(Optional.of(Fixtures.JOB_NAME_2), Optional.of(FAILED.getExitCode()), MAX_NUMBER_OF_EXECUTIONS_PER_JOB_NAME);
        assertThat(jes).containsExactly(Fixtures.je24, Fixtures.je23);
    }

}
