package com.go2wheel.mysqlbackup.convert;

import org.quartz.TriggerKey;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class TriggerNameToTrigger implements Converter<String, TriggerKey> {
	
	@Override
	public TriggerKey convert(String source) {
		String[] ss = source.split("-");
		if (ss.length == 2) {
			return new TriggerKey(ss[0], ss[1]);	
		}
		return new TriggerKey(source);
	}

}
