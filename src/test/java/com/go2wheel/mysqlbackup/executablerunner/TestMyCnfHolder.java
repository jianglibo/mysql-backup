package com.go2wheel.mysqlbackup.executablerunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.go2wheel.mysqlbackup.value.ConfigValue;
import com.go2wheel.mysqlbackup.value.ConfigValue.ConfigValueState;
import com.go2wheel.mysqlbackup.value.MyCnfFileLikeHolder;

public class TestMyCnfHolder {
	
	private Path fixture = Paths.get("fixtures", "mysql", "my.ini");
	
	private Path fixtureNoLogBin = Paths.get("fixtures", "mysql", "my-no-logbin.ini");
	
	@Test
	public void testCommentOutedLogbin() throws IOException {
		List<String> lines = Files.readAllLines(fixture);
		MyCnfFileLikeHolder mcf = new MyCnfFileLikeHolder(lines);

		ConfigValue cv = mcf.getConfigValue("log-bin");
		assertThat(cv.getState(), equalTo(ConfigValueState.COMMENT_OUTED));
		assertThat(cv.getValue(), equalTo("mysql-bin"));
		
		cv = mcf.getConfigValue("server-id");
		assertThat(cv.getState(), equalTo(ConfigValueState.EXIST));
		assertThat(cv.getValue(), equalTo("1"));

		cv = mcf.getConfigValue("server-id1");
		assertThat(cv.getState(), equalTo(ConfigValueState.NOT_EXIST));
		assertNull(cv.getValue());
		
		boolean changed = mcf.enableBinLog();
		
		assertTrue("should changed", changed);
		
		cv = mcf.getConfigValue("log-bin");
		assertThat(cv.getState(), equalTo(ConfigValueState.EXIST));
	}
	
	@Test
	public void testNoLogbin() throws IOException {
		List<String> lines = Files.readAllLines(fixtureNoLogBin);
		MyCnfFileLikeHolder mcf = new MyCnfFileLikeHolder(lines);

		ConfigValue cv = mcf.getConfigValue("log-bin");
		assertThat(cv.getState(), equalTo(ConfigValueState.NOT_EXIST));
		
		boolean changed = mcf.enableBinLog();
		
		assertTrue("should changed", changed);
		
		cv = mcf.getConfigValue("log-bin");
		assertThat(cv.getState(), equalTo(ConfigValueState.EXIST));
		assertThat(cv.getValue(), equalTo("mysql-bin"));
	}

	
	@Test
	public void tListAdd() {
		List<String> lines = new ArrayList<>();
		lines.add("a");
		lines.add(0, "abc");
		assertThat(lines.size(), equalTo(2));
		assertThat(lines.get(0), equalTo("abc"));
		
		lines = new ArrayList<>();
		lines.add("a");
		lines.add(1, "abc");
		assertThat(lines.size(), equalTo(2));
		assertThat(lines.get(0), equalTo("a"));
	}

}
