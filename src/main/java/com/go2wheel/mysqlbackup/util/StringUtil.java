package com.go2wheel.mysqlbackup.util;

import java.util.Arrays;
import java.util.List;

public class StringUtil {
	
	public static String NEWLINE_PTN = "[\\r\\n]+";
	
	public static List<String> splitLines(String str) {
		return Arrays.asList(str.split("\\R+"));
	}
	
	public static String getLastPartOfUrl(String url) {
		int i = url.lastIndexOf('/');
		return url.substring(i + 1);
	}

}
