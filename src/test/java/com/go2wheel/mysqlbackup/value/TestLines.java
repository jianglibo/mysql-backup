package com.go2wheel.mysqlbackup.value;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;

public class TestLines {
	
	
	@Test
	public void tFound() {
		Lines lines = new Lines(Arrays.asList("aa", "bb", "cc"));
		
		List<String> result = lines.findMatchAndReturnNextLines(Pattern.compile("bb"), 1);
		assertThat(result.size(), equalTo(1));
		assertThat(result.get(0), equalTo("cc"));
		
		result = lines.findMatchAndReturnNextLines(Pattern.compile("bb"), 10);
		assertThat(result.size(), equalTo(1));
		assertThat(result.get(0), equalTo("cc"));
		
		result = lines.findMatchAndReturnNextLines(Pattern.compile("xx"), 10);
		assertThat(result.size(), equalTo(0));
		
		result = lines.findMatchAndReturnNextLines(Pattern.compile("cc"), 10);
		assertThat(result.size(), equalTo(0));
		
		result = lines.findMatchAndReturnNextLines(Pattern.compile("aa"), 10);
		assertThat(result.size(), equalTo(2));
		assertThat(result, equalTo(Arrays.asList("bb", "cc")));
	



	}

}
