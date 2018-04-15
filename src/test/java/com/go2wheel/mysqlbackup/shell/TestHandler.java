package com.go2wheel.mysqlbackup.shell;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.shell.ResultHandler;
import org.springframework.shell.Shell;
import org.springframework.test.context.junit4.SpringRunner;

import com.go2wheel.mysqlbackup.tc.RhandlerCfg;

@SpringBootTest("spring.shell.interactive.enabled=false")
@RunWith(SpringRunner.class)
@Import(RhandlerCfg.class)
public class TestHandler {
	
	private Pattern ptn = Pattern.compile(".*?'(.*)'$");
	
	@Autowired
	private Shell shell;
	
	@Autowired
	@Qualifier("main")
	private ResultHandler resultHandler;
	
	@Test
	public void tRegex() {
		String s = "ab'cc'bc'";
		Matcher m = ptn.matcher(s);
		assertTrue(m.matches());
		assertThat(m.group(1), equalTo("cc'bc"));
	}

}
