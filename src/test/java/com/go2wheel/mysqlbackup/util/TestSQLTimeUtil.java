package com.go2wheel.mysqlbackup.util;

import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.junit.Test;


public class TestSQLTimeUtil {
	
	
	@Test
	public void tRecentDays() {
		Timestamp ts = SQLTimeUtil.recentDaysStartPoint(1);
		
		assertTrue(ts.toLocalDateTime().isBefore(LocalDateTime.now()));
		assertTrue(ts.toLocalDateTime().isAfter(LocalDateTime.now().minusDays(1)));
		
	}

}
