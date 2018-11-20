package com.go2wheel.mysqlbackup.powershell;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import java.nio.charset.Charset;

import org.junit.Test;

import com.go2wheel.mysqlbackup.util.PSUtil;
import com.go2wheel.mysqlbackup.value.ProcessExecResult;

public class TestPSUtil {
	
	@Test
	public void tStandardOutput() {
		Charset.forName("utf8");
		Charset.forName("utf-8");
		ProcessExecResult per = PSUtil.runPsCommandByCall("Get-Process | Select-Object -First 1 | ConvertTo-Json", Charset.forName("GB2312"));
		assertThat("stdout should be.", per.getStdOut().size(), greaterThan(0));
		assertThat("stderror should not be.", per.getStdError().size(), equalTo(0));
		
		per = PSUtil.runPsCommandByCall("Get-Process1 | Select-Object -First 1 | ConvertTo-Json", Charset.forName("GB2312"));
		assertThat("stdout should not be.", per.getStdOut().size(), equalTo(0));
		assertThat("stderror should be.", per.getStdError().size(), greaterThan(0));
	}

}
