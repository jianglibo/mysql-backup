package com.go2wheel.mysqlbackup.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.go2wheel.mysqlbackup.model.BaseModel;

public class ObjectUtil {

	private static Logger logger = LoggerFactory.getLogger(ObjectUtil.class);

	public static String dumpObjectAsMap(Object o) {
		Class<?> c = o.getClass();
		List<String> lines = new ArrayList<>();
		for (Field f : c.getDeclaredFields()) {
			try {
				f.setAccessible(true);
				lines.add(String.format("%s: %s", f.getName(), f.get(o)));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				ExceptionUtil.logErrorException(logger, e);
			}
		}
		return String.join("\n", lines);
	}
	
	public static Optional<String> getValueIfIsToListRepresentation(String toListRepresentation, String fieldName) {
		if (toListRepresentation.startsWith("[") && toListRepresentation.endsWith("]")) {
			String s = toListRepresentation.substring(1, toListRepresentation.length() - 2);
			String[] ss = s.split(", ");
			for(String par: ss) {
				String[] kv = par.split(": ", 2);
				if (kv.length == 2) {
					return Optional.ofNullable(kv[1]);
				}
			}
			return Optional.of("");
		} else {
			return Optional.empty();
		}
		
	}

	public static String toListRepresentation(Object o, String... fields) {
		Class<?> c = o.getClass();
		StringBuffer sb = new StringBuffer("[");
		Set<String> fset = new HashSet<>(Arrays.asList(fields));
		boolean hasIdField = fset.contains("id");
		fset.remove("id");
		String delim = "";

		if (fset.isEmpty()) {
			for (Field f : c.getDeclaredFields()) {
				try {
					sb.append(delim);
					f.setAccessible(true);
					sb.append(String.format("%s: %s", f.getName(), f.get(o)));
					delim = ", ";
				} catch (IllegalArgumentException | IllegalAccessException e) {
					ExceptionUtil.logErrorException(logger, e);
				}
			}
		} else {
			for (String fs : fset) {
				try {
					sb.append(delim);
					Field f = c.getDeclaredField(fs);
					f.setAccessible(true);
					sb.append(String.format("%s: %s", f.getName(), f.get(o)));
					delim = ", ";
				} catch (IllegalArgumentException | IllegalAccessException e) {
					ExceptionUtil.logErrorException(logger, e);
				} catch (NoSuchFieldException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				}
			}
		}
		if (hasIdField && o instanceof BaseModel) {
			sb.append(", id: ").append(((BaseModel)o).getId());
		}
		return sb.append("]").toString();
	}

}
