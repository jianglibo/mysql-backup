package com.go2wheel.mysqlbackup.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.Test;

import com.go2wheel.mysqlbackup.exception.StringReplaceException;
import com.go2wheel.mysqlbackup.value.LinuxLsl;

public class TestStringUtil {
	
	
	@Test
	public void tGetLastPartOfUrl() {
		assertThat(StringUtil.getLastPartOfUrl("https://dev.mysql.com/get/mysql57-community-release-el7-11.noarch.rpm"), equalTo("mysql57-community-release-el7-11.noarch.rpm"));
	}
	
	@Test
	public void tToLines() {
		List<String> lines = StringUtil.toLines("a=b,c=d");
		assertThat(lines, contains("a=b", "c=d"));
		
		lines = StringUtil.toLines("a=b=,,,,,c=d");
		assertThat(lines, contains("a=b=,,,,", "c=d"));
		
		lines = StringUtil.toLines("a=b=,,=,,,c=d");
		assertThat(lines, contains("a=b=,,=,,", "c=d"));

	}
	
	@Test
	public void tLinuxLsl() {
		String s = "-rw-r--r--. 1 root root 659238 Apr 27 13:48 /tmp/mysqldump.sql";
		String k = "-rw-r--r--. 1 root root 659238 Apr 27 14:16 /tmp/mysqldump.sql";
		Matcher m = LinuxLsl.LINUX_LSL_PATTERN.matcher(s);
		s = s.trim();
		assertTrue(m.matches());
		assertThat(m.group(1), equalTo("-rw-r--r--."));
		assertThat(m.group(2), equalTo("1"));
		assertThat(m.group(3), equalTo("root"));
		assertThat(m.group(4), equalTo("root"));
		assertThat(m.group(5), equalTo("659238"));
		assertThat(m.group(6), equalTo("Apr"));
		assertThat(m.group(7), equalTo("27"));
		assertThat(m.group(8), equalTo("13:48"));
		assertThat(m.group(9), equalTo("/tmp/mysqldump.sql"));
	}
	
	@Test
	public void tStringreplace() throws StringReplaceException {
		String s = "12年9月15日";
		Pattern ptn = Pattern.compile("(.*)月(.*?)(日)$"); // 
		Matcher m = ptn.matcher(s);
		assertTrue(m.matches());
		
		String[] asa;
		
		s = StringUtil.replacePattern(s, "^(.*?)(\\d+)(日)$", "%s%s%s", null, "16", null);
		assertThat(s, equalTo("12年9月16日"));
	}
	
	@Test
	public void js() throws ScriptException {
		List<ScriptEngineFactory> engins = new ScriptEngineManager().getEngineFactories();
		assertThat(engins.size(), equalTo(1));
		
		ScriptEngine engin = new ScriptEngineManager().getEngineByName("nashorn");
		Object o = engin.eval("'Hello';");
		assertThat(o, equalTo("Hello"));
	}
	
	@Test
	public void tParseInt() {
		String s = " 01";
		int i = StringUtil.parseInt(s);
		assertThat(i, equalTo(1));
		
		s = " 01m";
		i = StringUtil.parseInt(s);
		assertThat(i, equalTo(1));
		
		s = " 01%";
		i = StringUtil.parseInt(s);
		assertThat(i, equalTo(1));
		
		s = "a 01%";
		i = StringUtil.parseInt(s);
		assertThat(i, equalTo(0));
	}

	
	@Test
	public void testSize() {
		
	
		String ss = StringUtil.formatSize(1024L);
		assertThat(ss, equalTo("1024B"));
		
		ss = StringUtil.formatSize(1025L);
		assertThat(ss, equalTo("1.00KB"));
		
		ss = StringUtil.formatSize(1185L);
		assertThat(ss, equalTo("1.16KB"));
		
		ss = StringUtil.formatSize(1024*1024L);
		assertThat(ss, equalTo("1024.00KB"));
		
		ss = StringUtil.formatSize(1024*1024* 2L);
		assertThat(ss, equalTo("2.00MB"));
		
		ss = StringUtil.formatSize(1024*1024* 2 + 1024*6L);
		assertThat(ss, equalTo("2.01MB"));

		ss = StringUtil.formatSize(1024*1024* 2048L + 1024*1024* 50L);
		assertThat(ss, equalTo("2.05GB"));
		
		ss = StringUtil.formatSize(1024*1024* 1024* 2048L + 1024*1024* 50L);
		assertThat(ss, equalTo("2048.05GB"));

	}

}
