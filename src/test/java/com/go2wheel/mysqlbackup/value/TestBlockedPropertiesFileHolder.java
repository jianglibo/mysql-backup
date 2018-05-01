package com.go2wheel.mysqlbackup.value;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class TestBlockedPropertiesFileHolder {
	
	private Path fixture = Paths.get("fixtures", "mysql-community.repo");
	
	@Test
	public void t() throws IOException {
		BlockedPropertiesFileHolder bpf = new BlockedPropertiesFileHolder(Files.readAllLines(fixture));
		
		ConfigValue cv = bpf.getConfigValue("mysql55-community", "enabled");
		assertThat(cv.getValue(), equalTo("0"));
		
		bpf.setConfigValue(cv, "1");
		cv = bpf.getConfigValue("mysql55-community", "enabled");
		assertThat(cv.getValue(), equalTo("1"));
		
		cv = bpf.getConfigValue("mysql-connectors-community", "enabled");
		assertThat(cv.getValue(), equalTo("1"));
		
		
		bpf.setConfigValue(cv, "0");
		cv = bpf.getConfigValue("mysql-connectors-community", "enabled");
		assertThat(cv.getValue(), equalTo("0"));
	}

}
