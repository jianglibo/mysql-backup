package com.go2wheel.mysqlbackup.http;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.SettingsInDb;
import com.go2wheel.mysqlbackup.SpringBaseFort;

public class TestFileDownloader extends SpringBaseFort {
	
	@Autowired
	private FileDownloader fileDownloader;
	
	@Autowired
	private SettingsInDb settingsInDb;
	
	@Test
	public void t() throws ClientProtocolException, IOException {
		Path downloaded = fileDownloader.download("https://dev.mysql.com/get/mysql57-community-release-el7-11.noarch.rpm", settingsInDb.getDownloadPath().resolve("hello.rpm"));
		assertTrue(Files.isRegularFile(downloaded));
		assertTrue("file isn't empty.", Files.size(downloaded) > 0);
	}
	
	
	@Test
	public void tAsync() throws ClientProtocolException, IOException {
		CompletableFuture<Path> downloadedFu = fileDownloader.downloadAsync("https://dev.mysql.com/get/mysql57-community-release-el7-11.noarch.rpm", settingsInDb.getDownloadPath().resolve("hello.async.rpm"));
		
		Thread t = Thread.currentThread();
		
		downloadedFu.thenAccept(p -> {
			assertTrue(Files.isRegularFile(p));
			try {
				assertTrue("file isn't empty.", Files.size(p) > 0);
			} catch (IOException e) {
				e.printStackTrace();
			}
			t.interrupt();
		}).exceptionally(throwable -> {
			t.interrupt();
			return null;
		});
		
		try {
			Thread.sleep(1000000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
