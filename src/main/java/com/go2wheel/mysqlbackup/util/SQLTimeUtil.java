package com.go2wheel.mysqlbackup.util;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class SQLTimeUtil {
	
	public static Timestamp recentDaysStartPoint(int days) {
		LocalTime lc = LocalTime.MIDNIGHT;
		LocalDateTime ldt = lc.atDate(LocalDate.now().minusDays(days - 1));
		return Timestamp.valueOf(ldt);
	}

}
