package com.github.chrisgleissner.springbatchrest.util;

import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Date;

import org.quartz.CronScheduleBuilder;
import org.quartz.Trigger;

public class TriggerUtil {

	// Defined for clarity - passing in null is not obvious to what is happening
	// under the covers
	public static final String QUARTZ_DEFAULT_GROUP = null;

	public static Trigger triggerFor(String cronExpression, String jobName) {
		return triggerFor(cronExpression, jobName, QUARTZ_DEFAULT_GROUP);
	}

	public static Trigger triggerFor(String cronExpression, String jobName, String groupName) {
		CronScheduleBuilder builder = CronScheduleBuilder.cronSchedule(cronExpression);
		return newTrigger().withIdentity(jobName, groupName).withSchedule(builder).forJob(jobName, groupName).build();
	}

	public static Trigger triggerFor(Date dateToRun, String jobName) {
		return triggerFor(dateToRun, jobName, QUARTZ_DEFAULT_GROUP);
	}

	public static Trigger triggerFor(Date dateToRun, String jobName, String groupName) {
		return newTrigger().withIdentity(jobName, groupName).startAt(dateToRun).forJob(jobName, groupName).build();
	}
}
