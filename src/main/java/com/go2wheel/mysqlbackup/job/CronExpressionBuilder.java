package com.go2wheel.mysqlbackup.job;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.go2wheel.mysqlbackup.util.StringUtil;

public class CronExpressionBuilder {

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
	
	CronExpressionBuilder second(int second) { //0-59
		this.second = second + "";
		return this;
	}
	
	CronExpressionBuilder seconds(int...seconds) { //0-59
		this.second = String.join(",", IntStream.of(seconds).mapToObj(i -> i + "").collect(Collectors.toList()));
		return this;
	}
	
	CronExpressionBuilder secondRange(int start, int end) { //0-59
		this.second = start + "-" + end;
		return this;
	}
	
	CronExpressionBuilder secondIncreament(int start, int increament) { //0-59
		this.second = start + "/" + increament;
		return this;
	}
	
	CronExpressionBuilder minute(int minute) { //0-59
		this.minute = minute + "";
		return this;
	}
	
	CronExpressionBuilder minutes(int...minutes) { //0-59
		this.minute = String.join(",", IntStream.of(minutes).mapToObj(i -> i + "").collect(Collectors.toList()));
		return this;
	}
	
	CronExpressionBuilder minuteRange(int start, int end) { //0-59
		this.minute = start + "-" + end;
		return this;
	}
	
	CronExpressionBuilder minuteIncreament(int start, int increament) { //0-59
		this.minute = start + "/" + increament;
		return this;
	}
	
	CronExpressionBuilder hour(int hour) { //0-23
		this.hour = hour + "";
		return this;
	}
	
	CronExpressionBuilder hours(int...hours) { 
		this.hour = String.join(",", IntStream.of(hours).mapToObj(i -> i + "").collect(Collectors.toList()));
		return this;
	}
	
	CronExpressionBuilder hourRange(int start, int end) {
		this.hour = start + "-" + end;
		return this;
	}
	
	CronExpressionBuilder hourIncreament(int start, int increament) {
		this.hour = start + "/" + increament;
		return this;
	}
	
	
	CronExpressionBuilder dayOfMonth(int dayOfMonth) { // 1- 31
		this.dayOfMonth = dayOfMonth + "";
		return this;
	}
	
	CronExpressionBuilder lastNdayOfMonth(int offsetToLast) { // 1- 31
		if (offsetToLast == 0) {
			this.dayOfMonth = "L";
		} else {
			this.dayOfMonth = "L" + "-" + offsetToLast;
		}
		return this;
	}

	
	
	CronExpressionBuilder dayOfMonths(int...dayOfMonths) { 
		this.dayOfMonth = String.join(",", IntStream.of(dayOfMonths).mapToObj(i -> i + "").collect(Collectors.toList()));
		return this;
	}
	
	CronExpressionBuilder dayOfMonthRange(int start, int end) {
		this.dayOfMonth = start + "-" + end;
		return this;
	}
	
	CronExpressionBuilder dayOfMonthIncreament(int start, int increament) {
		this.dayOfMonth = start + "/" + increament;
		return this;
	}
	
	
	CronExpressionBuilder month(int month) { // 1-12 or JAN-DEC
		this.month = month + "";
		return this;
	}
	
	CronExpressionBuilder months(int...months) { 
		this.month = String.join(",", IntStream.of(months).mapToObj(i -> i + "").collect(Collectors.toList()));
		return this;
	}
	
	CronExpressionBuilder monthRange(int start, int end) {
		this.month = start + "-" + end;
		return this;
	}
	
	CronExpressionBuilder monthIncreament(int start, int increament) {
		this.month = start + "/" + increament;
		return this;
	}
	
	
	
	CronExpressionBuilder dayOfWeek(int dayOfWeek) { //1-7 or SUN-SAT
		this.dayOfWeek = dayOfWeek + "";
		return this;
	}
	
	CronExpressionBuilder dayOfWeeks(int...dayOfWeeks) { 
		this.dayOfWeek = String.join(",", IntStream.of(dayOfWeeks).mapToObj(i -> i + "").collect(Collectors.toList()));
		return this;
	}
	
	CronExpressionBuilder dayOfWeekRange(int start, int end) {
		this.dayOfWeek = start + "-" + end;
		return this;
	}
	
	CronExpressionBuilder dayOfWeekIncreament(int start, int increament) {
		this.dayOfWeek = start + "/" + increament;
		return this;
	}
	
	CronExpressionBuilder lastDayOfWeekInMonth(int dayOfWeek) {
		this.dayOfWeek =  dayOfWeek + "L";
		return this;
	}
	
	CronExpressionBuilder dayOfWeekInMonth(int dayOfWeek, int whichWeekInMonth) {
		this.dayOfWeek =  dayOfWeek + "#" + whichWeekInMonth;
		return this;
	}
	
	CronExpressionBuilder year(String year) { //empty, 1970-2099
		this.year = year;
		return this;
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