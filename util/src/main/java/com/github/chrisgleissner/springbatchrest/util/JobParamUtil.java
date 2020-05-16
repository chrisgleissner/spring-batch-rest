package com.github.chrisgleissner.springbatchrest.util;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;

public class JobParamUtil {

	public static JobParameters convertRawToJobParams(Map<String, Object> properties) {
		return new JobParameters(convertRawToParamMap(properties));
	}

	public static Map<String, JobParameter> convertRawToParamMap(Map<String, Object> properties) {
		return Optional.ofNullable(properties).orElse(emptyMap()).entrySet().stream()
				.collect(toMap(Map.Entry::getKey, e -> createJobParameter(e.getValue())));
	}

	public static JobParameter createJobParameter(Object value) {
		if (value instanceof Date)
			return new JobParameter((Date) value);
		else if (value instanceof Long)
			return new JobParameter((Long) value);
		else if (value instanceof Double)
			return new JobParameter((Double) value);
		else
			return new JobParameter("" + value);
	}
}
