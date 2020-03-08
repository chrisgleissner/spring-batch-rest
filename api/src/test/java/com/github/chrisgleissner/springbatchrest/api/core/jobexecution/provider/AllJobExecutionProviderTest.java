package com.github.chrisgleissner.springbatchrest.api.core.jobexecution.provider;

import com.github.chrisgleissner.springbatchrest.api.core.Fixtures;
import org.junit.Before;
import org.mockito.Mock;
import org.springframework.batch.core.explore.JobExplorer;

import static com.github.chrisgleissner.springbatchrest.api.core.Fixtures.configureForJobExecutionsService;
import static com.github.chrisgleissner.springbatchrest.api.core.Fixtures.configureMock;

public class AllJobExecutionProviderTest extends AbstractJobExecutionProviderTest {

    @Mock
    private JobExplorer jobExplorer;

    private AllJobExecutionProvider provider;

    @Before
    public void setUp() {
        Fixtures.configureMock(jobExplorer);
        Fixtures.configureForJobExecutionsService(jobExplorer);
        provider = new AllJobExecutionProvider(jobExplorer);
    }

    @Override
    protected JobExecutionProvider provider() {
        return provider;
    }
}