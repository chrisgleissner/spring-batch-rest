package com.github.chrisgleissner.springbatchrest.util.core.property;

import com.github.chrisgleissner.springbatchrest.util.core.JobConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * @deprecated see notes at {@link JobPropertyResolvers#JobProperties}
 */
@Deprecated
@Slf4j @Component
public class JobPropertyResolvers implements Consumer<JobExecution> {

    /**
     * @deprecated Accessing {@link org.springframework.batch.core.Job} properties via this singleton is not safe for
     * asynchronously executing the same <code>Job</code> multiple times with different properties. In this case a {@link JobExecution}
     * may incorrectly use the properties of another, concurrently running <code>JobExecution</code> of the same <code>Job</code>.
     *
     * <p>
     * Instead, it is recommended to access job properties via either {@link StepExecution#getJobParameters()} or by annotating your
     * Spring-wired job beans with <code>@Value("#{jobParameters['key']}")</code>. You can get a handle of a <code>StepExecution</code>
     * by implementing {@link org.springframework.batch.core.StepExecutionListener} or extending
     * {@link com.github.chrisgleissner.springbatchrest.util.core.tasklet.StepExecutionListenerTasklet}.
     * </p>
     *
     * <p>For convenience, when using
     * {@link com.github.chrisgleissner.springbatchrest.util.core.JobBuilder#createJob(String, Consumer)} to build a job,
     * the returned {@link PropertyResolver} will first resolve against job properties, then against Spring properties.
     * </p>
     *
     * @see com.github.chrisgleissner.springbatchrest.util.core.JobBuilder#createJob(String, Consumer)
     */
    @Deprecated
    public static JobPropertyResolvers JobProperties;

    private Environment environment;
    private Map<String, JobPropertyResolver> resolvers = new ConcurrentHashMap<>();

    @Autowired
    public JobPropertyResolvers(Environment environment, JobExecutionAspect jobExecutionAspect) {
        this.environment = environment;
        jobExecutionAspect.register(this);
        JobProperties = this;
    }

    public PropertyResolver of(String jobName) {
        JobPropertyResolver jobPropertyResolver = resolvers.get(jobName);
        return jobPropertyResolver == null ? environment : jobPropertyResolver;
    }

    public void started(JobConfig jobConfig) {
        String jobName = jobConfig.getName();
        JobPropertyResolver resolver = new JobPropertyResolver(jobConfig, environment);
        resolvers.put(jobName, resolver);
        log.info("Enabled {}", resolver);
    }

    @Override
    public void accept(JobExecution je) {
        if (!je.isRunning()) {
            JobPropertyResolver resolver = resolvers.remove(je.getJobInstance().getJobName());
            if (resolver != null)
                log.info("Disabled {}", je.getJobInstance().getJobName());
        }
    }
}
