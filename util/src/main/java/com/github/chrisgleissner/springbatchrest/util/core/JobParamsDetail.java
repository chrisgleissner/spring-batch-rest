package com.github.chrisgleissner.springbatchrest.util.core;

import java.util.Map;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.impl.JobDetailImpl;
import org.quartz.utils.Key;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Extension class to add JobParameters to a JobDetail for scheduled execution
 * with JobParameters.
 * 
 * @author theJeff77
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class JobParamsDetail extends JobDetailImpl {

	private static final long serialVersionUID = -4813776846767160965L;
	private Map<String, Object> rawJobParameters;

	// Constructor to do a deep copy all data from JobDetail into this subclass.
	// Based off of JobBuilder's construction code.
	public JobParamsDetail(JobDetail jobDetail) {

		this.setJobClass(jobDetail.getJobClass());
		this.setDescription(jobDetail.getDescription());
		if (jobDetail.getKey() == null)
			this.setKey(new JobKey(Key.createUniqueName(null), null));
		this.setKey(jobDetail.getKey());
		this.setDurability(jobDetail.isDurable());
		this.setRequestsRecovery(jobDetail.requestsRecovery());

		if (!jobDetail.getJobDataMap().isEmpty())
			this.setJobDataMap(jobDetail.getJobDataMap());
	}

	public JobParamsDetail(JobDetail jobDetail, Map<String, Object> rawJobParameters) {
		this(jobDetail);
		this.rawJobParameters = rawJobParameters;
	}
}
