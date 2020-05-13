package com.github.chrisgleissner.springbatchrest.util.quartz.config;

import com.github.chrisgleissner.springbatchrest.util.core.config.AdHocBatchConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


@Configuration 
@ComponentScan(basePackages = {
		"com.github.chrisgleissner.springbatchrest.util.quartz"
		})
@Import({AdHocBatchConfig.class, SchedulerConfig.class})
public class AdHocSchedulerConfig {

}
