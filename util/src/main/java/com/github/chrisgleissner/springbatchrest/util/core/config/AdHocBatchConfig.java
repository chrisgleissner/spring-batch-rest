package com.github.chrisgleissner.springbatchrest.util.core.config;

import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;

import javax.sql.DataSource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration 
@ComponentScan(basePackages = { 
		"com.github.chrisgleissner.springbatchrest.util.core",
		"com.github.chrisgleissner.springbatchrest.util.core.property"
		})
@EnableBatchProcessing 
@EnableAspectJAutoProxy
public class AdHocBatchConfig extends DefaultBatchConfigurer {

    @Autowired
    private JobRepository jobRepository;

    // TODO: These seem to be unused. Consider removal after Chris's feedback.
    @Autowired
    private JobLocator jobLocator;

    @Autowired
    private JobLauncher jobLauncher;

    private ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public void setDataSource(DataSource dataSource) {
        // override to do not set datasource even if a datasource exist.
        // initialize will use a Map based JobRepository (instead of database)
    }

    public JobLauncher getJobLauncher() {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(new ConcurrentTaskExecutor(executorService));
        return jobLauncher;
    }
}
