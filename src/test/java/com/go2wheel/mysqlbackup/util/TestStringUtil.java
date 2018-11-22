package com.go2wheel.mysqlbackup.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.Test;

import com.go2wheel.mysqlbackup.exception.StringReplaceException;
import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;

public class TestStringUtil {
	
	@Test
	public void tCaseConvert() {
		Converter<String, String> c1 = CaseFormat.LOWER_UNDERSCORE.converterTo(CaseFormat.LOWER_HYPHEN)
				.andThen(CaseFormat.UPPER_UNDERSCORE.converterTo(CaseFormat.LOWER_HYPHEN))
				.andThen(CaseFormat.UPPER_CAMEL.converterTo(CaseFormat.LOWER_HYPHEN))
				.andThen(CaseFormat.UPPER_CAMEL.converterTo(CaseFormat.LOWER_HYPHEN))
				.andThen(CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.LOWER_HYPHEN));
		
		String kk = StringUtil.convertToLowerHyphen("ab_dddLick");
		assertThat(kk, equalTo("ab-ddd-lick"));
		
		kk = StringUtil.convertToLowerHyphen("ab-dddLick");
		assertThat(kk, equalTo("ab-dddlick"));
		
		kk = StringUtil.convertToLowerHyphen("AB-DDD");
		assertThat(kk, equalTo("ab-ddd"));
		
		kk = StringUtil.convertToLowerHyphen("AbDDD");
		assertThat(kk, equalTo("ab-d-d-d"));

	}
	
	@Test
	public void tExtractExecutable() {
		String cc = "& 'C:/Program Files/WinRAR/Rar.exe' a -ms %s %s";
		String executable = StringUtil.extractExecutable(cc);
		assertThat(executable, equalTo("C:/Program Files/WinRAR/Rar.exe"));

		cc = " \"C:/Program Files/WinRAR/Rar.exe\" a -ms %s %s";
		executable = StringUtil.extractExecutable(cc);
		assertThat(executable, equalTo("C:/Program Files/WinRAR/Rar.exe"));
		
		cc = " 'C:/Program Files/WinRAR/Rar.exe' a -ms %s %s";
		executable = StringUtil.extractExecutable(cc);
		assertThat(executable, equalTo("C:/Program Files/WinRAR/Rar.exe"));

		cc = " C:/Program Files/WinRAR/Rar.exe' a -ms %s %s";
		executable = StringUtil.extractExecutable(cc);
		assertThat(executable, equalTo("C:/Program"));
	}
	
	@Test
	public void tEscapePowershellString() {
		String origin = "a'bbcde";
		String after = StringUtil.espacePowershellString(origin);
		assertThat(after, equalTo("\"a'bbcde\""));
		
		origin = "a\"bbcde";
		after = StringUtil.espacePowershellString(origin);
		assertThat(after, equalTo("\"a`\"bbcde\""));

	}
	
	@Test
	public void tQuotation() {
		String origin = "a'bbcde";
		
		String after = StringUtil.quotation(origin, true);
		
		assertThat(after, equalTo("'a\\'bbcde'"));
		
		after = StringUtil.quotation(origin, false);
		
		assertThat(after, equalTo("\"a\'bbcde\""));
		
		
		origin = "a\"bbcde";
		
		after = StringUtil.quotation(origin, true);
		
		assertThat(after, equalTo("'a\"bbcde'"));
		
		after = StringUtil.quotation(origin, false);
		
		assertThat(after, equalTo("\"a\\\"bbcde\""));
		System.out.println(after);
		
	}
	
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
		
		Map<String, String> m = new TreeMap<>();
		m.put("a", "b");
		m.put("c", "d");
		
		lines = StringUtil.toLines(m);
		assertThat(lines, contains("a=b", "c=d"));

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
