package com.github.chrisgleissner.springbatchrest.api.jobexecution.provider;

import org.junit.Before;
import org.mockito.Mock;
import org.springframework.batch.core.explore.JobExplorer;

import static com.github.chrisgleissner.springbatchrest.api.Fixtures.configureForJobExecutionsService;
import static com.github.chrisgleissner.springbatchrest.api.Fixtures.configureMock;

public class AllJobExecutionProviderTest extends AbstractJobExecutionProviderTest {

    @Mock
    private JobExplorer jobExplorer;

    private AllJobExecutionProvider provider;

    @Before
    public void setUp() {
        configureMock(jobExplorer);
        configureForJobExecutionsService(jobExplorer);
        provider = new AllJobExecutionProvider(jobExplorer);
    }

    @Override
    protected JobExecutionProvider provider() {
        return provider;
    }
}