package com.github.chrisgleissner.springbatchrest.api.jobexecution;

import com.github.chrisgleissner.springbatchrest.api.jobexecution.provider.CachedJobExecutionProvider;
import com.github.chrisgleissner.springbatchrest.util.adhoc.AdHocStarter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.chrisgleissner.springbatchrest.api.Fixtures.*;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest
public class JobExecutionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CachedJobExecutionProvider cachedJobExecutionProvider;

    @MockBean
    private JobExplorer jobExplorer;

    @MockBean
    private JobRegistry jobRegistry;

    @MockBean
    private AdHocStarter adHocStarter;

    private static AtomicBoolean initialized = new AtomicBoolean();

    @Before
    public void setUp() {
        if (initialized.compareAndSet(false, true)) {
            configureMock(jobExplorer);
            configureForJobExecutionsService(jobExplorer);
            when(jobExplorer.getJobExecution(je11.getId())).thenReturn(je11);

            configureMock(jobRegistry);
            configureForJobExecutionsService(cachedJobExecutionProvider);
        }
    }

    @Test
    public void jobExecutions() throws Exception {
        mockMvc.perform(get("/jobExecutions"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..jobExecution", hasSize(5)));
    }

    @Test
    public void successfulJobExecutions() throws Exception {
        mockMvc.perform(get("/jobExecutions?exitCode=COMPLETED"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..jobExecution", hasSize(3)));
    }

    @Test
    public void failedJobExecutions() throws Exception {
        mockMvc.perform(get("/jobExecutions?exitCode=FAILED"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..jobExecution", hasSize(3)));
    }

    @Test
    public void successfulJobExecutionsPerJob() throws Exception {
        mockMvc.perform(get("/jobExecutions?jobName=j2&exitCode=COMPLETED"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..jobExecution", hasSize(2)));
    }

    @Test
    public void successfulJobExecutionsPerJobAndLimited() throws Exception {
        mockMvc.perform(get("/jobExecutions?jobName=j2&exitCode=COMPLETED&limitPerJob=1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..jobExecution", hasSize(1)));
    }
}