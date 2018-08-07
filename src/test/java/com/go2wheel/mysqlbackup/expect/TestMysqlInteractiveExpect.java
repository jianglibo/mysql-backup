package com.go2wheel.mysqlbackup.expect;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

import com.go2wheel.mysqlbackup.util.MysqlUtil;

public class TestMysqlInteractiveExpect {
	
	private Path fixture = Paths.get("fixtures", "mysql", "sqloutput", "showdatabases.txt");
	private Path fixture1 = Paths.get("fixtures", "mysql", "sqloutput", "showdatabases2.txt");
	
	
	@Test
	public void t() throws IOException {
		List<String> allLines = MysqlUtil.getColumnValues(Files.readAllLines(fixture), 0);
		List<String> databases = allLines.subList(1, allLines.size());
		assertThat(databases.size(), equalTo(3));
		assertThat(databases, contains("information_schema", "mysql", "performance_schema"));
		
		allLines = MysqlUtil.getColumnValues(Files.readAllLines(fixture), 1);
		assertTrue(allLines.isEmpty());
	}
	
	
	@Test
	public void t1() throws IOException {
		List<String> allLines = MysqlUtil.getColumnValues(Files.readAllLines(fixture1), 1);
		List<String> databases = allLines.subList(1, allLines.size());
		assertThat(databases.size(), equalTo(3));
		assertThat(databases, contains("a", "b", "c"));
		
		allLines = MysqlUtil.getColumnValues(Files.readAllLines(fixture1), 1);
		assertTrue(allLines.size() == 4);
	}
			

}
