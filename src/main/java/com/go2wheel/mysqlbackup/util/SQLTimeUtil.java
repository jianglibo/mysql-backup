package com.go2wheel.mysqlbackup.util;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

public class SQLTimeUtil {
	
	public static Timestamp recentDaysStartPoint(int days) {
		LocalTime lc = LocalTime.MIDNIGHT;
		LocalDateTime ldt = lc.atDate(LocalDate.now().minusDays(days - 1));
		return Timestamp.valueOf(ldt);
	}

	public static String formatDate(Date date) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		return df.format(date);
	}

}
