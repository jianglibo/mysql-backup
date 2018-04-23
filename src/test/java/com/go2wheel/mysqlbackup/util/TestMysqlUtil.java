package com.go2wheel.mysqlbackup.util;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.go2wheel.mysqlbackup.sshj.SshBaseFort;
import com.go2wheel.mysqlbackup.value.ConfigValue;
import com.go2wheel.mysqlbackup.value.MyCnfHolder;
import com.go2wheel.mysqlbackup.yml.YamlInstance;
import com.go2wheel.mysqlbackup.value.ConfigValue.ConfigValueState;

public class TestMysqlUtil extends SshBaseFort {
	
	private MysqlUtil mysqlUtil;
	
	@Before
	public void before() throws IOException {
		super.before();
		mysqlUtil = new MysqlUtil();
		mysqlUtil.setSshClientFactory(sshClientFactory);
	}
	
	
	@Test
	public void t() {
		MyCnfHolder mcf = mysqlUtil.getMycnf(demoInstance);
		ConfigValue cv = mcf.getConfigValue("datadir");
		assertThat(cv.getState(), equalTo(ConfigValueState.EXIST));
		assertThat(cv.getValue(), equalTo("/var/lib/mysql"));
		assertFalse(mcf.getLines().isEmpty());
		
		demoInstance.setMycnfContent(mcf.getLines());
		String oneline = YamlInstance.INSTANCE.getYaml().dumpAsMap(demoInstance);
		System.out.println(oneline);
		Optional<String> aline = StringUtil.splitLines(oneline).stream().filter(line -> line.indexOf("# For advice on how to change settings please see") != -1).findFirst();
		assertTrue(aline.isPresent());
	}

}
