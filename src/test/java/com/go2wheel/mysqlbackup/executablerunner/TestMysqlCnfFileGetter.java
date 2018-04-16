package com.go2wheel.mysqlbackup.executablerunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

import com.go2wheel.mysqlbackup.value.ConfigValue;
import com.go2wheel.mysqlbackup.value.ConfigValue.ConfigValueState;
import com.go2wheel.mysqlbackup.value.MyCnfHolder;

public class TestMysqlCnfFileGetter {
	
	private Path fixture = Paths.get("fixtures", "mysql", "my.ini");
	
	@Test
	public void t() throws IOException {
		List<String> lines = Files.readAllLines(fixture);
		MyCnfHolder mcf = new MyCnfHolder(lines);

		ConfigValue cv = mcf.getConfigValue("log-bin");
		assertThat(cv.getState(), equalTo(ConfigValueState.COMMENT_OUTED));
		assertThat(cv.getValue(), equalTo("mysql-bin"));
		
		cv = mcf.getConfigValue("server-id");
		assertThat(cv.getState(), equalTo(ConfigValueState.EXIST));
		assertThat(cv.getValue(), equalTo("1"));

		cv = mcf.getConfigValue("server-id1");
		assertThat(cv.getState(), equalTo(ConfigValueState.NOT_EXIST));
		assertNull(cv.getValue());
		
		mcf.enableBinLog();
		
	}

}
