package com.go2wheel.mysqlbackup.util;

import org.quartz.CronTrigger;
import org.quartz.Trigger;

public class ToStringFormat {
	
	public static String formatTriggerOutput(Trigger trigger) {
		String tostr = trigger.toString();
		if (CronTrigger.class.isAssignableFrom(trigger.getClass())) {
			tostr = tostr + ", cron: " + ((CronTrigger) trigger).getCronExpression();
		}
		return tostr;

	}

}
