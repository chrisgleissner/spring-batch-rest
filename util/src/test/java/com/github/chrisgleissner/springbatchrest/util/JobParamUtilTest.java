package com.github.chrisgleissner.springbatchrest.util;

import java.util.Date;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobParameter;

public class JobParamUtilTest {

	@Test
	public void testObjectConversionHappy() {
		Date date = new Date();
		JobParameter dateParam = JobParamUtil.createJobParameter(date);
		Assertions.assertEquals(dateParam.getValue(), date);

		Long longVar = new Long(1234);
		JobParameter longParam = JobParamUtil.createJobParameter(longVar);
		Assertions.assertEquals(longParam.getValue(), longVar);

		Double doubleVar = new Double(123.123);
		JobParameter doubleParam = JobParamUtil.createJobParameter(doubleVar);
		Assertions.assertEquals(doubleParam.getValue(), doubleVar);
	}
}
