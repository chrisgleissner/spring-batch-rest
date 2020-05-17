package com.github.chrisgleissner.springbatchrest.util.core.property;

import com.github.chrisgleissner.springbatchrest.util.core.JobConfig;
import org.springframework.core.env.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static java.util.Collections.emptyMap;

/**
 * @deprecated see notes at {@link JobPropertyResolvers#JobProperties}
 */
@Deprecated
public class JobPropertyResolver extends PropertySourcesPropertyResolver {
    private final JobConfig jobConfig;

    public JobPropertyResolver(JobConfig jobConfig, Environment env) {
        super(propertySources(jobConfig, env));
        this.jobConfig = jobConfig;
    }

    private static PropertySources propertySources(JobConfig jobConfig, Environment env) {
        MutablePropertySources propertySources = new MutablePropertySources();
        Map<String, Object> jobProperties = new HashMap<>(Optional.ofNullable(jobConfig.getProperties()).orElse(emptyMap()));
        propertySources.addFirst(new MapPropertySource(jobConfig.getName(), jobProperties));
        ((AbstractEnvironment) env).getPropertySources().forEach(propertySources::addLast);
        return propertySources;
    }

    public String toString() {
        return String.format("Properties for job %s: %s", jobConfig.getName(), jobConfig.getProperties());
    }
}
