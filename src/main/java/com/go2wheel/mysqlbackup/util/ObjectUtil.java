package com.go2wheel.mysqlbackup.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.go2wheel.mysqlbackup.model.BaseModel;

public class ObjectUtil {

	private static Logger logger = LoggerFactory.getLogger(ObjectUtil.class);
	
	public static List<Field> getFields(Class<?> c) {
		List<Class<?>> clazzList = new ArrayList<>();
		clazzList.add(c);
		while((c = c.getSuperclass()) != Object.class) {
			clazzList.add(c);
		}
		
		return clazzList.stream().flatMap(oc -> Arrays.stream(oc.getDeclaredFields())).filter(f -> {
			f.setAccessible(true);
			int m = f.getModifiers();
			return !Modifier.isStatic(m);
		}).collect(Collectors.toList());
	}
	
	public static Optional<Field> getField(Class<?> c, String fieldName) {
		
		List<Class<?>> clazzList = new ArrayList<>();
		clazzList.add(c);
		while((c = c.getSuperclass()) != null) {
			clazzList.add(c);
		}
		
		return clazzList.stream().flatMap(oc -> Arrays.stream(oc.getDeclaredFields())).filter(f -> {
			f.setAccessible(true);
			int m = f.getModifiers();
			return !Modifier.isStatic(m);
		}).filter(f -> fieldName.equals(f.getName())).findAny();
	}

	public static String dumpObjectAsMap(Object o) {
		Class<?> c = o.getClass();
		return getFields(c).stream().map(f -> {
			try {
				return String.format("%s: %s", f.getName(), f.get(o));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				return null;
			}
		}).filter(Objects::nonNull).collect(Collectors.joining("\n"));
	}
	
	public static Optional<String> getValueIfIsToListRepresentation(String toListRepresentation, String fieldName) {
		if (toListRepresentation.startsWith("[") && toListRepresentation.endsWith("]")) {
			String s = toListRepresentation.substring(1, toListRepresentation.length() - 1);
			String[] ss = s.split(", ");
			for(String par: ss) {
				String[] kv = par.split(": ", 2);
				if (kv.length == 2) {
					String ktrim = kv[0].trim();
					String vtrim = kv[1].trim();
					if (ktrim.equals(fieldName)) {
						return Optional.of(vtrim);
					}
					
				}
			}
			return Optional.of("");
		} else {
			return Optional.empty();
		}
	}
	
	public static String getValueWetherIsToListRepresentationOrNot(String toListRepresentation, String fieldName) {
		if (toListRepresentation.startsWith("[") && toListRepresentation.endsWith("]")) {
			String s = toListRepresentation.substring(1, toListRepresentation.length() - 2);
			String[] ss = s.split(", ");
			for(String par: ss) {
				String[] kv = par.split(": ", 2);
				return kv[1];
			}
			return "";
		} else {
			return toListRepresentation;
		}
	}
	
	
	private static Field findField(Class<?> c, String fn) {
		Class<?> pc = c;
		while(pc != Object.class) {
			try {
				Field f = pc.getDeclaredField(fn);
				return f;
			} catch (NoSuchFieldException | SecurityException e) {
			}
			pc = pc.getSuperclass();
		}
		return null;
	}

	public static String toListRepresentation(Object o, String... fields) {
		Class<?> c = o.getClass();
		StringBuffer sb = new StringBuffer("[");
		Set<String> fset = new HashSet<>(Arrays.asList(fields));
		boolean hasIdField = fset.contains("id");
		fset.remove("id");
		String delim = "";

		if (fset.isEmpty()) {
			List<Field> fds = getFields(c);
			for (Field f : fds) {
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
					Field f = findField(c, fs);
					if (f != null) {
						f.setAccessible(true);
						sb.append(String.format("%s: %s", f.getName(), f.get(o)));
						delim = ", ";
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					ExceptionUtil.logErrorException(logger, e);
				} catch (SecurityException e) {
					ExceptionUtil.logErrorException(logger, e);
				}
			}
		}
		if (hasIdField && o instanceof BaseModel) {
			sb.append(", id: ").append(((BaseModel)o).getId());
		}
		return sb.append("]").toString();
	}

	public static void setValue(Field field, Object o, String value) throws NumberFormatException, IllegalArgumentException, IllegalAccessException {
		if (field.getType() == int.class || field.getType() == Integer.class) {
			field.set(o, Integer.valueOf(value));
		} else {
			field.set(o, value);
		}
	}

}
