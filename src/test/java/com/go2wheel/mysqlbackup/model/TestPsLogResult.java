package com.go2wheel.mysqlbackup.model;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.go2wheel.mysqlbackup.SpringBaseFort;

public class TestPsLogResult  extends SpringBaseFort {
	
	@Value("${fort.logfolder}")
	private String logfolder;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Test
	public void tDump() throws IOException {
		Path p = Paths.get(logfolder,"mysql", "Dump");
		Path f = Files.list(p).findFirst().get();
		PsMysqldumpResult result = objectMapper.readValue(f.toFile(), PsMysqldumpResult.class);
		assertTrue(result.isSuccess());
	}
	
	@Test
	public void tFlush() throws IOException {
		Path p = Paths.get(logfolder, "mysql", "FlushLogs");
		Path f = Files.list(p).findFirst().get();
		PsMysqlflushResult result = objectMapper.readValue(f.toFile(), PsMysqlflushResult.class);
		assertTrue(result.isSuccess());
		assertThat(result.getResult().size(), greaterThan(0));
	}
	
	
	@Test
	public void tBorgArchive() throws IOException {
		Path p = Paths.get(logfolder,"borg", "ArchiveAndDownload");
		Path f = Files.list(p).findFirst().get();
		PsBorgAchiveResult result = objectMapper.readValue(f.toFile(), PsBorgAchiveResult.class);
		assertTrue(result.isSuccess());
		assertThat(result.getDownload().getTotal().getFiles().size(), greaterThan(0));
		assertThat(result.getDownload().getFailed().getFiles().size(), equalTo(0));
		assertThat(result.getDownload().getCopied().getFiles().size(), greaterThan(0));

	}
	
	@Test
	public void tBorgPrune() throws IOException {
		Path p = Paths.get(logfolder, "borg", "PruneAndDownload");
		Path f = Files.list(p).findFirst().get();
		PsBorgAchiveResult result = objectMapper.readValue(f.toFile(), PsBorgAchiveResult.class);
		assertTrue(result.isSuccess());
		assertThat(result.getDownload().getTotal().getFiles().size(), greaterThan(0));
		assertThat(result.getDownload().getFailed().getFiles().size(), equalTo(0));
		assertThat(result.getDownload().getCopied().getFiles().size(), greaterThan(0));
	}


}
