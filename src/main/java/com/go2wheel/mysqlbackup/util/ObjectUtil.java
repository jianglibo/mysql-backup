package com.go2wheel.mysqlbackup.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectUtil {
	
	private static Logger logger = LoggerFactory.getLogger(ObjectUtil.class);
	
	public static String dumpObjectAsMap(Object o) {
		Class<?> c = o.getClass();
		List<String> lines = new ArrayList<>();
		for(Field f : c.getDeclaredFields()) {
			try {
				f.setAccessible(true);
				lines.add(String.format("%s: %s", f.getName(), f.get(o)));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				ExceptionUtil.logErrorException(logger, e);
			}
		}
		return String.join("\n", lines);
	}

}
