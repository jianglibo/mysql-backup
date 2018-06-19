package com.go2wheel.mysqlbackup.convert;

import org.quartz.JobKey;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class JobkeyNameToJobKey implements Converter<String, JobKey> {
	
	@Override
	public JobKey convert(String source) {
		String[] ss = source.split("-");
		if (ss.length == 2) {
			return new JobKey(ss[0], ss[1]);	
		}
		return new JobKey(source);
	}
}
