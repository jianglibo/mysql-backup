package com.go2wheel.mysqlbackup.http;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import com.go2wheel.mysqlbackup.jsch.SshBaseFort;

public class TestFileDownloader extends SshBaseFort {
	
	@Test
	public void t() throws ClientProtocolException, IOException {
		FileDownloader fd = new FileDownloader();
		fd.setAppSettings(appSettings);
		
		Path downloaded = fd.download("https://dev.mysql.com/get/mysql57-community-release-el7-11.noarch.rpm");
		assertTrue(Files.isRegularFile(downloaded));
		
		assertTrue("file isn't empty.", Files.size(downloaded) > 0);
	}
}
