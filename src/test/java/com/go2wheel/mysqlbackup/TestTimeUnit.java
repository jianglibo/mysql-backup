package com.go2wheel.mysqlbackup;

import static org.hamcrest.CoreMatchers.equalTo;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.go2wheel.mysqlbackup.util.ObjectUtil;
import com.go2wheel.mysqlbackup.value.BorgDownloadRepoStatus;

public class TestTimeUnit {
	
	
	@Test
	public void t() {
		long l = TimeUnit.MINUTES.toSeconds(1L);
		assertThat(l, equalTo(60L));
		
		l = TimeUnit.MILLISECONDS.toSeconds(1000L);
		assertThat(l, equalTo(1L));
	}
	
	
	@Test
	public void tObjectUtil() {
		String s = ObjectUtil.dumpObjectAsMap(new BorgDownloadRepoStatus());
		
		assertTrue(s.contains("totalFiles:"));
		assertTrue(s.contains("downloadFiles:"));
		assertTrue(s.contains("totalBytes:"));
		assertTrue(s.contains("downloadBytes:"));
		
	}
}
