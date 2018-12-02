package com.go2wheel.mysqlbackup;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class TestTimeUnit {
	
	@Test
	public void t() {
		long l = TimeUnit.MINUTES.toSeconds(1L);
		assertThat(l, equalTo(60L));
		
		l = TimeUnit.MILLISECONDS.toSeconds(1000L);
		assertThat(l, equalTo(1L));
	}
	
	

}
