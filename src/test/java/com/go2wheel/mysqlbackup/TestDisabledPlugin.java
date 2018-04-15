package com.go2wheel.mysqlbackup;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest("spring.shell.interactive.enabled=false")
@RunWith(SpringRunner.class)
public class TestDisabledPlugin {
	
	@Autowired
	private DisabledPlugin disabledPlugin;

	@Test
	public void tPropertyBean() {
		assertThat(disabledPlugin.getIgnorecheckers().size(), greaterThan(0));
		assertThat(disabledPlugin.getPathadjusters().size(), greaterThan(0));
		assertThat(disabledPlugin.getPerformers().size(), greaterThan(0));
	}
}
