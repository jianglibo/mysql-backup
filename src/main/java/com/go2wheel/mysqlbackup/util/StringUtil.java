package com.go2wheel.mysqlbackup.util;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class StringUtil {
	
	public static String NEWLINE_PTN = "[\\r\\n]+";
	
	public static List<String> splitLines(String str) {
		return Arrays.asList(str.split("\\R+"));
	}
	
	public static Optional<String> notEmptyValue(String maybeEmpty) {
		if (maybeEmpty == null || maybeEmpty.trim().isEmpty()) {
			return Optional.empty();
		} else {
			return Optional.of(maybeEmpty);
		}
	}
	
	public static String getLastPartOfUrl(String url) {
		int i = url.lastIndexOf('/');
		return url.substring(i + 1);
	}

}
