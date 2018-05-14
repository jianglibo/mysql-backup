package com.go2wheel.mysqlbackup.job;

import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.go2wheel.mysqlbackup.exception.InvalidCronExpressionFieldException;
import com.go2wheel.mysqlbackup.util.StringUtil;

public class CronExpressionBuilder {
	
	public static enum CronExpressionField {
		SECOND, MINUTE, HOUR, DAY_OF_MONTH, MONTH, DAY_OF_WEEK, YEAR
	}

	private String second;

	private String minute;

	private String hour;

	private String dayOfMonth;

	private String month;

	private String dayOfWeek;

	private String year;
	
	public String build() {
		if (!StringUtil.hasAnyNonBlankWord(second)) {
			second = "0";
		}
		
		if (!StringUtil.hasAnyNonBlankWord(minute)) {
			minute = "0";
		}

		if (!StringUtil.hasAnyNonBlankWord(hour)) {
			hour = "0";
		}

		if (!StringUtil.hasAnyNonBlankWord(dayOfMonth)) {
			dayOfMonth = "?";
		}

		if (!StringUtil.hasAnyNonBlankWord(month)) {
			month = "*";
		}

		if (!StringUtil.hasAnyNonBlankWord(dayOfWeek)) {
			dayOfWeek = "*";
		}
		
		if (!"?".equals(dayOfMonth)) {
			dayOfWeek = "?";
		}

		if (!StringUtil.hasAnyNonBlankWord(year)) {
			year = "";
		}
		
		return String.format("%s %s %s %s %s %s %s", second, minute, hour, dayOfMonth, month, dayOfWeek, year).trim();
	}
	
	public CronExpressionBuilder second(String second) {
		this.second = second;
		return this;
	}
	
	public CronExpressionBuilder seconds(int...seconds) { //0-59
		this.second = String.join(",", IntStream.of(seconds).mapToObj(i -> i + "").collect(Collectors.toList()));
		return this;
	}
	
	public CronExpressionBuilder secondRange(int start, int end) { //0-59
		this.second = start + "-" + end;
		return this;
	}
	
	public CronExpressionBuilder secondIncreament(int start, int increament) { //0-59
		this.second = start + "/" + increament;
		return this;
	}
	
	public CronExpressionBuilder minute(String minute) {
		this.minute = minute;
		return this;
	}

	
	public CronExpressionBuilder minutes(int...minutes) { //0-59
		this.minute = String.join(",", IntStream.of(minutes).mapToObj(i -> i + "").collect(Collectors.toList()));
		return this;
	}
	
	public CronExpressionBuilder minuteRange(int start, int end) { //0-59
		this.minute = start + "-" + end;
		return this;
	}
	
	public CronExpressionBuilder minuteIncreament(int start, int increament) { //0-59
		this.minute = start + "/" + increament;
		return this;
	}

	public CronExpressionBuilder hour(String hour) { 
		this.hour = hour;
		return this;
	}
	
	public CronExpressionBuilder hours(int...hours) { 
		this.hour = String.join(",", IntStream.of(hours).mapToObj(i -> i + "").collect(Collectors.toList()));
		return this;
	}
	
	public CronExpressionBuilder hourRange(int start, int end) {
		this.hour = start + "-" + end;
		return this;
	}
	
	public CronExpressionBuilder hourIncreament(int start, int increament) {
		this.hour = start + "/" + increament;
		return this;
	}
	
	
	public CronExpressionBuilder lastNdayOfMonth(int offsetToLast) { // 1- 31
		if (offsetToLast == 0) {
			this.dayOfMonth = "L";
		} else {
			this.dayOfMonth = "L" + "-" + offsetToLast;
		}
		return this;
	}
	
	public CronExpressionBuilder dayOfMonth(String dayOfMonth) { 
		this.dayOfMonth = dayOfMonth;
		return this;
	}
	
	public CronExpressionBuilder dayOfMonths(int...dayOfMonths) { 
		this.dayOfMonth = String.join(",", IntStream.of(dayOfMonths).mapToObj(i -> i + "").collect(Collectors.toList()));
		return this;
	}
	
	public CronExpressionBuilder dayOfMonthRange(int start, int end) {
		this.dayOfMonth = start + "-" + end;
		return this;
	}
	
	public CronExpressionBuilder dayOfMonthIncreament(int start, int increament) {
		this.dayOfMonth = start + "/" + increament;
		return this;
	}
	
	public CronExpressionBuilder month(String month) { 
		this.month = month;
		return this;
	}
	
	public CronExpressionBuilder months(int...months) { 
		this.month = String.join(",", IntStream.of(months).mapToObj(i -> i + "").collect(Collectors.toList()));
		return this;
	}
	
	public CronExpressionBuilder monthRange(int start, int end) {
		this.month = start + "-" + end;
		return this;
	}
	
	public CronExpressionBuilder monthIncreament(int start, int increament) {
		this.month = start + "/" + increament;
		return this;
	}
	
	public CronExpressionBuilder dayOfWeek(String dayOfWeek) { 
		this.dayOfWeek = dayOfWeek;
		return this;
	}

	
	public CronExpressionBuilder dayOfWeeks(int...dayOfWeeks) { 
		this.dayOfWeek = String.join(",", IntStream.of(dayOfWeeks).mapToObj(i -> i + "").collect(Collectors.toList()));
		return this;
	}
	
	public CronExpressionBuilder dayOfWeekRange(int start, int end) {
		this.dayOfWeek = start + "-" + end;
		return this;
	}
	
	public CronExpressionBuilder dayOfWeekIncreament(int start, int increament) {
		this.dayOfWeek = start + "/" + increament;
		return this;
	}
	
	public CronExpressionBuilder lastDayOfWeekInMonth(int dayOfWeek) {
		this.dayOfWeek =  dayOfWeek + "L";
		return this;
	}
	
	public CronExpressionBuilder dayOfWeekInMonth(int dayOfWeek, int whichWeekInMonth) {
		this.dayOfWeek =  dayOfWeek + "#" + whichWeekInMonth;
		return this;
	}
	
	public CronExpressionBuilder year(String year) { //empty, 1970-2099
		this.year = year;
		return this;
	}
	
	
	public static void validCronField(CronExpressionField cef, String fieldValue) throws InvalidCronExpressionFieldException {
		int startLimit = 0, endLimit = 0;
		InvalidCronExpressionFieldException e = new InvalidCronExpressionFieldException(cef, fieldValue);
		switch (cef) {
		case SECOND:
			startLimit = 0;
			endLimit = 59;
			if ("*".equals(fieldValue)) return;
			break;
		case MINUTE:
			startLimit = 0;
			endLimit = 59;
			if ("*".equals(fieldValue)) return;
			break;
		case HOUR:
			startLimit = 0;
			endLimit = 23;
			if ("*".equals(fieldValue)) return;
			break;
		case DAY_OF_MONTH:
			startLimit = 1;
			endLimit = 31;
			if ("*".equals(fieldValue) || "?".equals(fieldValue)) return;
			break;
		case MONTH:
			startLimit = 1;
			endLimit = 12;
			if ("*".equals(fieldValue)) return;
			break;
		case DAY_OF_WEEK:
			startLimit = 1;
			endLimit = 7;
			if ("*".equals(fieldValue)  || "?".equals(fieldValue)) return;
			break;
		case YEAR:
			startLimit = 1970;
			endLimit = 2099;
			if ("*".equals(fieldValue)) return;
			break;
		default:
			break;
		}
		final int startFinal = startLimit;
		final int endFinal = endLimit;
		
		Matcher m = StringUtil.ALL_DIGITS_PTN.matcher(fieldValue);
		if (m.matches()) {
			int v = Integer.valueOf(fieldValue);
			if (v < startFinal && v > endFinal) {
				throw e;
			}
		} else if (fieldValue.indexOf(',') != -1) {
			try {
				boolean b = Stream.of(fieldValue.split(",")).map(Integer::valueOf).anyMatch(i -> i < startFinal || i > endFinal);
				if (b) {
					throw e;
				}
			} catch (Exception e1) {
				throw e;
			}
		} else if (fieldValue.indexOf('-') != -1) {
			String[] ss = fieldValue.split("-");
			if (ss.length != 2) {
				throw e;
			} else {
				int start = Integer.valueOf(ss[0]);
				int end = Integer.valueOf(ss[1]);
				if (start < startFinal || end > endFinal || end <= start) {
					throw e;
				}
			}
		} else if (fieldValue.indexOf('/') != -1) {
			String[] ss = fieldValue.split("/");
			if (ss.length != 2) {
				throw e;
			} else {
				int start = Integer.valueOf(ss[0]);
				int increament = Integer.valueOf(ss[1]);
				if (start < startFinal || increament > endFinal || increament < 2) {
					throw e;
				}
			}
		}
	}

}

//@formatter:off
/*
* (“all values”) - used to select all values within a field. For example, “” in the minute field means *“every minute”.

? (“no specific value”) - useful when you need to specify something in one of the two fields in which the character is allowed, but not the other. For example, if I want my trigger to fire on a particular day of the month (say, the 10th), but don’t care what day of the week that happens to be, I would put “10” in the day-of-month field, and “?” in the day-of-week field. See the examples below for clarification.

- - used to specify ranges. For example, “10-12” in the hour field means “the hours 10, 11 and 12”.

, - used to specify additional values. For example, “MON,WED,FRI” in the day-of-week field means “the days Monday, Wednesday, and Friday”.

/ - used to specify increments. For example, “0/15” in the seconds field means “the seconds 0, 15, 30, and 45”. And “5/15” in the seconds field means “the seconds 5, 20, 35, and 50”. You can also specify ‘/’ after the ‘’ character - in this case ‘’ is equivalent to having ‘0’ before the ‘/’. ‘1/3’ in the day-of-month field means “fire every 3 days starting on the first day of the month”.

L (“last”) - has different meaning in each of the two fields in which it is allowed. For example, the value “L” in the day-of-month field means “the last day of the month” - day 31 for January, day 28 for February on non-leap years. If used in the day-of-week field by itself, it simply means “7” or “SAT”. But if used in the day-of-week field after another value, it means “the last xxx day of the month” - for example “6L” means “the last friday of the month”. You can also specify an offset from the last day of the month, such as “L-3” which would mean the third-to-last day of the calendar month. When using the ‘L’ option, it is important not to specify lists, or ranges of values, as you’ll get confusing/unexpected results.

W (“weekday”) - used to specify the weekday (Monday-Friday) nearest the given day. As an example, if you were to specify “15W” as the value for the day-of-month field, the meaning is: “the nearest weekday to the 15th of the month”. So if the 15th is a Saturday, the trigger will fire on Friday the 14th. If the 15th is a Sunday, the trigger will fire on Monday the 16th. If the 15th is a Tuesday, then it will fire on Tuesday the 15th. However if you specify “1W” as the value for day-of-month, and the 1st is a Saturday, the trigger will fire on Monday the 3rd, as it will not ‘jump’ over the boundary of a month’s days. The ‘W’ character can only be specified when the day-of-month is a single day, not a range or list of days.

The 'L' and 'W' characters can also be combined in the day-of-month field to yield 'LW', which translates to *"last weekday of the month"*.
# - used to specify “the nth” XXX day of the month. For example, the value of “6#3” in the day-of-week field means “the third Friday of the month” (day 6 = Friday and “#3” = the 3rd one in the month). Other examples: “2#1” = the first Monday of the month and “4#5” = the fifth Wednesday of the month. Note that if you specify “#5” and there is not 5 of the given day-of-week in the month, then no firing will occur that month.
The legal characters and the names of months and days of the week are not case sensitive. MON is the same as mon.
*/