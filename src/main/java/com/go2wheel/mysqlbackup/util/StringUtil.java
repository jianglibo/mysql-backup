package com.go2wheel.mysqlbackup.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.go2wheel.mysqlbackup.exception.StringReplaceException;

public class StringUtil {
	
	public static String NEWLINE_PTN = "[\\r\\n]+";
	
	public static Pattern ALL_DIGITS_PTN = Pattern.compile("^\\d+$");
	
	public static Pattern NUMBER_HEADED = Pattern.compile("\\s*(\\d+).*");
	
	public static List<String> splitLines(String str) {
		return Arrays.asList(str.split("\\R+"));
	}
	
	public static Optional<String> notEmptyValue(String maybeEmpty) {
		if (maybeEmpty == null || maybeEmpty.trim().isEmpty() || "null".equals(maybeEmpty)) {
			return Optional.empty();
		} else {
			return Optional.of(maybeEmpty);
		}
	}
	
	public static String getLastPartOfUrl(String url) {
		int i = url.lastIndexOf('/');
		return url.substring(i + 1);
	}
	
	public static int parseInt(String numberHeaded) {
		Matcher m = NUMBER_HEADED.matcher(numberHeaded);
		if (m.matches()) {
			return Integer.valueOf(m.group(1));
		} else {
			return 0;
		}
	}
	
	
	public static String[] matchGroupValues(Matcher m) {
		int c = m.groupCount();
		String[] ss = new String[c];
		for(int i = 0; i<c; i++) {
			ss[i] = m.group(i + 1);
		}
		return ss;
	}
	
	public static boolean hasAnyNonBlankWord(String s) {
		return s != null && !(s.trim().isEmpty());
	}
	
	public static Object[] matchGroupReplace(Matcher m, Object...replaces) {
		Object[] oo = matchGroupValues(m);
		int l = oo.length;
		for(int i = 0; i< l; i++) {
			if (replaces[i] == null) {
				continue;
			} else {
				oo[i] = replaces[i];
			}
		}
		return oo;
	}
	
//	private static String placeHoderPtn = "\\(.*?\\)";
	
	public static String replacePattern(String origin, String pattern,String fmt, Object...replaces) throws StringReplaceException {
		Pattern ptn = Pattern.compile(pattern); // 
		Matcher m = ptn.matcher(origin);
		if (!m.matches()) {
			throw new StringReplaceException(origin, pattern);
		}
		if (m.groupCount() != replaces.length) {
			throw new StringReplaceException(origin, pattern, replaces);
		}
//		String fmt = pattern.replaceAll(placeHoderPtn, "%s");
		return String.format(fmt, matchGroupReplace(m, replaces));
		
	}
	
	
	public static String inputstreamToString(InputStream inputStream) {
		try {
			ByteArrayOutputStream result = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int length;
			while ((length = inputStream.read(buffer)) != -1) {
			    result.write(buffer, 0, length);
			}
			// StandardCharsets.UTF_8.name() > JDK 7
			return result.toString("UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}


}
