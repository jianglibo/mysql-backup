package com.go2wheel.mysqlbackup.value;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.go2wheel.mysqlbackup.util.StringUtil;

public class LinuxLsl {

	public static final Pattern LINUX_LSL_PATTERN = Pattern.compile("^([^\\s]+)\\s+(\\d+)\\s+([^ ]+)\\s+([^ ]+)\\s+(\\d+)\\s+([^ ]+)\\s+(\\d+)\\s+([^ ]+)\\s+(.*)$");
	
	public void setMd5ByMd5sumOutput(String expectOut) {
		Optional<String[]> md5pair = StringUtil.splitLines(expectOut).stream().map(l -> l.trim()).map(l -> l.split("\\s+")).filter(pair -> pair.length == 2).filter(pair -> pair[1].equals(filename) && pair[0].length() == 32).findAny();
		if (md5pair.isPresent()) {
			setMd5(md5pair.get()[0]);
		}
	}
	
	public LinuxLsl(Matcher m) {
		pm = m.group(1);
		numberOfLinks = Integer.valueOf(m.group(2));
		owner= m.group(3);
		group = m.group(4);
		size = Long.valueOf(m.group(5));
		lastModified = String.format("%s %s %S", m.group(6), m.group(7), m.group(8));
		filename = m.group(9);
	}
	
	
	public static Optional<LinuxLsl> matchAndReturnLinuxLsl(String oneLine) {
		String s = oneLine.trim();
		Matcher m = LinuxLsl.LINUX_LSL_PATTERN.matcher(s);
		boolean success = m.matches();
		if (success) {
			return Optional.of(new LinuxLsl(m));
		}
		return Optional.empty();
	}
	
	public static Optional<LinuxLsl> matchAndReturnLinuxLsl(List<String> withMd5) {
		if (withMd5.size() == 1) {
			return matchAndReturnLinuxLsl(withMd5.get(0));	
		} else if (withMd5.size() == 2) {
			Optional<LinuxLsl> r = matchAndReturnLinuxLsl(withMd5.get(0));
			r.get().setMd5ByMd5sumOutput(withMd5.get(1));
			return r;
		} else {
			return Optional.empty();
		}
	}
	
	@Override
	public String toString() {
		return String.format("%s: %s\n%s: %s\n%s: %s", "filename", filename, "size", size, "md5", md5);
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
