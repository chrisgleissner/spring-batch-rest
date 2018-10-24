package com.github.chrisgleissner.springbatchrest.api.jobexecution;

import com.github.chrisgleissner.springbatchrest.util.adhoc.AdHocStarter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static com.github.chrisgleissner.springbatchrest.api.MockSetup.configureMock;
import static com.github.chrisgleissner.springbatchrest.api.MockSetup.configureMockForJobExecutionsService;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest
public class JobExecutionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobExplorer jobExplorer;

    @MockBean
    private JobRegistry jobRegistry;

    @MockBean
    private AdHocStarter adHocStarter;

    @Before
    public void setUp() throws NoSuchJobException {
        configureMock(jobExplorer);
        configureMock(jobRegistry);
        configureMockForJobExecutionsService(jobExplorer);
    }

    @Test
    public void jobExecutions() throws Exception {
        mockMvc.perform(get("/jobExecutions"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..jobExecution", hasSize(6)));
    }
}