package com.go2wheel.mysqlbackup.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.go2wheel.mysqlbackup.value.MycnfFileHolder;

public class TestDeleteHmLogBinIndexFile {
	
    @Rule
    public TemporaryFolder tfolder= new TemporaryFolder();
	
	@Test
	public void t() throws IOException {
		Path p = tfolder.newFile(MycnfFileHolder.DEFAULT_LOG_BIN_BASE_NAME + ".index").toPath();
		
		Files.write(p, "abc".getBytes());
		
		assertThat(Files.list(tfolder.getRoot().toPath()).count(), equalTo(1L));
		
		InputStream is = Files.newInputStream(p);
		
		Files.delete(p);
		
		assertThat(Files.list(tfolder.getRoot().toPath()).count(), equalTo(0L));
		
	}

}
