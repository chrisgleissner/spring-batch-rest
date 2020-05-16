package com.github.chrisgleissner.springbatchrest.util;

import java.util.Date;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DateUtilTest {

	@Test
	public void testLocalDateConversion() {
		Date now = new Date();
		LocalDateTime localDateTime = DateUtil.localDateTime(now);
		Instant ldtInstant = localDateTime.toInstant(OffsetDateTime.now().getOffset());

		Assertions.assertEquals(ldtInstant, now.toInstant());
	}
}
