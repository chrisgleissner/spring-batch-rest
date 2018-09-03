package com.github.chrisgleissner.springbatchrest.api.job;

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

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobExplorer jobExplorer;

    @Before
    public void setUp() throws NoSuchJobException {
        configureMock(jobExplorer);
    }

    @Test
    public void jobs() throws Exception {
        mockMvc.perform(get("/job"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(2)));
    }

}
