package com.github.chrisgleissner.springbatchrest.api.jobexecution;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static com.github.chrisgleissner.springbatchrest.api.MockSetup.configureMock;
import static com.github.chrisgleissner.springbatchrest.api.MockSetup.configureMockForJobExecutionsService;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class JobExecutionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobExplorer jobExplorer;

    @Before
    public void setUp() throws NoSuchJobException {
        configureMock(jobExplorer);
        configureMockForJobExecutionsService(jobExplorer);
    }

    @Test
    public void jobExecutions() throws Exception {
        mockMvc.perform(get("/jobExecution"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..jobExecution", hasSize(6)));
    }
}