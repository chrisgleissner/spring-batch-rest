package com.github.chrisgleissner.springbatchrest.util.adhoc.property;

import com.github.chrisgleissner.springbatchrest.util.adhoc.JobConfig;
import org.springframework.core.env.*;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

public class JobPropertyResolver extends PropertySourcesPropertyResolver {

    private final JobConfig jobConfig;

    public JobPropertyResolver(JobConfig jobConfig, Environment env) {
        super(propertySources(jobConfig, env));
        this.jobConfig = jobConfig;
    }

    private static PropertySources propertySources(JobConfig jobConfig, Environment env) {
        MutablePropertySources propertySources = new MutablePropertySources();
        Map<String, Object> jobProperties = new HashMap<>(jobConfig.getProperties());
        propertySources.addFirst(new MapPropertySource(jobConfig.getName(), jobProperties));
        ((AbstractEnvironment) env).getPropertySources().forEach(ps -> propertySources.addLast(ps));
        return propertySources;
    }

    public String toString() {
        return String.format("Properties for job %s: %s", jobConfig.getName(), jobConfig.getProperties());
    }
}
