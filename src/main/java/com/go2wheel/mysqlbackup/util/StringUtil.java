package com.go2wheel.mysqlbackup.util;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.go2wheel.mysqlbackup.exception.StringReplaceException;

public class StringUtil {
	
	public static class LinuxFileInfo {
		
		public static final Pattern LINUX_LSL_PATTERN = Pattern.compile("^([^\\s]+)\\s+(\\d+)\\s+([^ ]+)\\s+([^ ]+)\\s+(\\d+)\\s+([^ ]+)\\s+(\\d+)\\s+([^ ]+)\\s+(.*)$");
		
		public void setMd5ByMd5sumOutput(String expectOut) {
			Optional<String[]> md5pair = splitLines(expectOut).stream().map(l -> l.trim()).map(l -> l.split("\\s+")).filter(pair -> pair.length == 2).filter(pair -> pair[1].equals(filename) && pair[0].length() == 32).findAny();
			if (md5pair.isPresent()) {
				setMd5(md5pair.get()[0]);
			}
		}
		
		public LinuxFileInfo(Matcher m) {
			pm = m.group(1);
			numberOfLinks = Integer.valueOf(m.group(2));
			owner= m.group(3);
			group = m.group(4);
			size = Long.valueOf(m.group(5));
			lastModified = String.format("%s %s %S", m.group(6), m.group(7), m.group(8));
			filename = m.group(9);
		}
		
		private String pm;
		private int numberOfLinks;
		private String owner;
		private String group;
		private long size;
		private String lastModified;
		private String filename;
		
		private String md5;

		public String getMd5() {
			return md5;
		}
		public void setMd5(String md5) {
			this.md5 = md5;
		}
		public String getPm() {
			return pm;
		}
		public void setPm(String pm) {
			this.pm = pm;
		}
		public int getNumberOfLinks() {
			return numberOfLinks;
		}
		public void setNumberOfLinks(int numberOfLinks) {
			this.numberOfLinks = numberOfLinks;
		}
		public String getOwner() {
			return owner;
		}
		public void setOwner(String owner) {
			this.owner = owner;
		}
		public String getGroup() {
			return group;
		}
		public void setGroup(String group) {
			this.group = group;
		}
		public long getSize() {
			return size;
		}
		public void setSize(long size) {
			this.size = size;
		}
		public String getLastModified() {
			return lastModified;
		}
		public void setLastModified(String lastModified) {
			this.lastModified = lastModified;
		}
		public String getFilename() {
			return filename;
		}
		public void setFilename(String filename) {
			this.filename = filename;
		}
	}
	
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
	
	public static Optional<LinuxFileInfo> matchLinuxLsl(String oneLine) {
		String s = oneLine.trim();
		Matcher m = LinuxFileInfo.LINUX_LSL_PATTERN.matcher(s);
		boolean success = m.matches();
		if (success) {
			return Optional.of(new LinuxFileInfo(m));
		}
		return Optional.empty();
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
	
	public static String[] matchGroupReplace(Matcher m, String...replaces) {
		String[] oo = matchGroupValues(m);
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
	
	private static String placeHoderPtn = "\\(.*?\\)";
	
	public static String replacePattern(String origin, String pattern,String fmt, String...replaces) throws StringReplaceException {
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


}
