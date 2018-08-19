package com.go2wheel.mysqlbackup.value;

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
import com.go2wheel.mysqlbackup.value.MycnfFileHolder;

public class TestMyCnfHolder {
	
	private Path fixture = Paths.get("fixtures", "mysql", "my.ini");
	
	private Path fixtureNoLogBin = Paths.get("fixtures", "mysql", "my-no-logbin.ini");
	
	@Test
	public void testCommentOutedLogbin() throws IOException {
		List<String> lines = Files.readAllLines(fixture);
		MycnfFileHolder mcf = new MycnfFileHolder(lines);

		ConfigValue cv = mcf.getConfigValue(MycnfFileHolder.MYSQLD_BLOCK, MycnfFileHolder.MYSQLD_LOG_BIN_KEY);
		assertThat(cv.getState(), equalTo(ConfigValueState.NOT_EXIST));
		
		cv = mcf.getConfigValue("server-id");
		assertThat(cv.getState(), equalTo(ConfigValueState.EXIST));
		assertThat(cv.getValue(), equalTo("1"));

		cv = mcf.getConfigValue("server-id1");
		assertThat(cv.getState(), equalTo(ConfigValueState.NOT_EXIST));
		assertNull(cv.getValue());
		
		int linenum = mcf.getLines().size();
		
		boolean changed = mcf.enableBinLog();
		
		assertThat("should add a new line.", mcf.getLines().size(), equalTo(linenum + 1));
		
		assertTrue("should changed", changed);
		
		cv = mcf.getConfigValue(MycnfFileHolder.MYSQLD_BLOCK, MycnfFileHolder.MYSQLD_LOG_BIN_KEY);
		assertThat(cv.getState(), equalTo(ConfigValueState.EXIST));
		
		mcf.disableBinLog();
		cv = mcf.getConfigValue(MycnfFileHolder.MYSQLD_BLOCK, MycnfFileHolder.MYSQLD_LOG_BIN_KEY);
		assertThat(cv.getState(), equalTo(ConfigValueState.COMMENT_OUTED));
	}
	
	@Test
	public void testNoLogbin() throws IOException {
		List<String> lines = Files.readAllLines(fixtureNoLogBin);
		MycnfFileHolder mcf = new MycnfFileHolder(lines);

		ConfigValue cv = mcf.getConfigValue(MycnfFileHolder.MYSQLD_LOG_BIN_KEY);
		assertThat(cv.getState(), equalTo(ConfigValueState.NOT_EXIST));
		
		boolean changed = mcf.enableBinLog();
		
		assertTrue("should changed", changed);
		
		cv = mcf.getConfigValue(MycnfFileHolder.MYSQLD_LOG_BIN_KEY);
		assertThat(cv.getState(), equalTo(ConfigValueState.EXIST));
		assertThat(cv.getValue(), equalTo(MycnfFileHolder.DEFAULT_LOG_BIN_BASE_NAME));
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
