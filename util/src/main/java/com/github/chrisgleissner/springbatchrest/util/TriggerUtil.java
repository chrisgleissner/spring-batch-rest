package com.github.chrisgleissner.springbatchrest.util;

import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Date;
import java.util.TimeZone;

import org.quartz.CronScheduleBuilder;
import org.quartz.Trigger;

public class TriggerUtil {

	// Also defined in AdHocScheduler. TODO: consider using Quartz default group.
	private static final String GROUP_NAME = "group";

	public static Trigger triggerFor(String cronExpression, String jobName, TimeZone timeZone) {
		return triggerFor(cronExpression, jobName, timeZone, GROUP_NAME);
	}

	public static Trigger triggerFor(String cronExpression, String jobName, TimeZone timeZone, String groupName) {

		CronScheduleBuilder builder = CronScheduleBuilder.cronSchedule(cronExpression);

		if (timeZone != null) {
			builder = builder.inTimeZone(timeZone);
		}

		return newTrigger().withIdentity(jobName, groupName).withSchedule(builder).forJob(jobName, groupName).build();
	}

	public static Trigger triggerFor(Date dateToRun, String jobName) {
		return triggerFor(dateToRun, jobName, GROUP_NAME);
	}

	public static Trigger triggerFor(Date dateToRun, String jobName, String groupName) {
		return newTrigger().withIdentity(jobName, groupName).startAt(dateToRun).forJob(jobName, groupName).build();
	}
}
