package com.go2wheel.mysqlbackup.aop;

import java.util.concurrent.TimeUnit;

public interface TimeCost {

	void setStartTime(long startTime);

	void setEndTime(long endTime);

	long getStartTime();

	long getEndTime();

	default String getTimeCost(TimeUnit unit) {
		long delta = getEndTime() - getStartTime();
		switch (unit) {
		case SECONDS:
			return TimeUnit.MILLISECONDS.toSeconds(delta) + "s";
		case DAYS:
			return TimeUnit.MILLISECONDS.toDays(delta) + unit.toString();
		case HOURS:
			return TimeUnit.MILLISECONDS.toHours(delta) + unit.toString();
		case MICROSECONDS:
			return TimeUnit.MILLISECONDS.toMicros(delta) + unit.toString();
		case MILLISECONDS:
			return TimeUnit.MILLISECONDS.toMillis(delta) + "ms";
		case MINUTES:
			return TimeUnit.MILLISECONDS.toMinutes(delta) + unit.toString();
		case NANOSECONDS:
			return TimeUnit.MILLISECONDS.toNanos(delta) + unit.toString();
		default:
			break;
		}
		return "";
	}

}
