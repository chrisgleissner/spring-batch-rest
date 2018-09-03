package com.github.chrisgleissner.springbatchrest.api.jobdetail;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.*;
import org.quartz.impl.JobDetailImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import com.github.chrisgleissner.springbatchrest.util.adhoc.QuartzJobLauncher;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class JobDetailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Scheduler scheduler;

    @Before
    public void setUp() throws SchedulerException {
        when(scheduler.getJobGroupNames()).thenReturn(newArrayList("g1"));
        when(scheduler.getJobKeys(any())).thenReturn(Sets.newHashSet(new JobKey("g1", "jd1"), new JobKey("g1", "jd2")));
        doReturn(Lists.newArrayList((Trigger) newTrigger()
                .withSchedule(cronSchedule("0/1 * * * * ?"))
                .build())).when(scheduler).getTriggersOfJob(any());

        when(scheduler.getJobDetail(any(JobKey.class))).thenAnswer(i -> {
            JobDetailImpl jobDetail = new JobDetailImpl();
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put(QuartzJobLauncher.JOB_NAME, ((JobKey) i.getArgument(0)).getName());
            jobDetail.setJobDataMap(jobDataMap);
            return jobDetail;
        });
    }

    @Test
    public void jobDetail() throws Exception {
        mockMvc.perform(get("/jobDetail/g1/j1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..jobDetail", hasSize(1)));
    }

    @Test
    public void jobDetails() throws Exception {
        mockMvc.perform(get("/jobDetail"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..jobDetail", hasSize(2)));
    }
}