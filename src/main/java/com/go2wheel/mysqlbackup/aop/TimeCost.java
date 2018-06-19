package com.go2wheel.mysqlbackup.aop;

import java.util.concurrent.TimeUnit;

import com.go2wheel.mysqlbackup.util.StringUtil;

public interface TimeCost {

	void setStartTime(long startTime);

	void setEndTime(long endTime);

	long getStartTime();

	long getEndTime();

	default String getTimeCost(TimeUnit unit) {
		long delta = getEndTime() - getStartTime();
		return StringUtil.getTimeCost(delta, unit);
	}

}
