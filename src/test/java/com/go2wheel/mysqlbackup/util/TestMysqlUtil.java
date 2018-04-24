package com.go2wheel.mysqlbackup.util;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import com.go2wheel.mysqlbackup.sshj.SshBaseFort;
import com.go2wheel.mysqlbackup.value.ConfigValue;
import com.go2wheel.mysqlbackup.value.MyCnfFileLikeHolder;
import com.go2wheel.mysqlbackup.yml.YamlInstance;
import com.go2wheel.mysqlbackup.value.ConfigValue.ConfigValueState;

public class TestMysqlUtil extends SshBaseFort {
	
	private MysqlUtil mysqlUtil;
	
	@Before
	public void before() throws IOException {
		super.before();
		mysqlUtil = new MysqlUtil();
		mysqlUtil.setSshClientFactory(sshClientFactory);
		mysqlUtil.setAppSettings(appSettings);
	}
	
	
	@Test
	public void tFetchMyCnfAndSave() throws IOException {
		MyCnfFileLikeHolder mcf = mysqlUtil.getMycnf(demoBox);
		ConfigValue cv = mcf.getConfigValue("datadir");
		assertThat(cv.getState(), equalTo(ConfigValueState.EXIST));
		assertThat(cv.getValue(), equalTo("/var/lib/mysql"));
		assertFalse(mcf.getLines().isEmpty());
		
		demoBox.getMysqlInstance().setMycnfContent(mcf.getLines());
		String oneline = YamlInstance.INSTANCE.getYaml().dumpAsMap(demoBox);
		System.out.println(oneline);
		Optional<String> aline = StringUtil.splitLines(oneline).stream().filter(line -> line.indexOf("# For advice on how to change settings please see") != -1).findFirst();
		assertTrue(aline.isPresent());
		mysqlUtil.writeDescription(demoBox);
	}
	
	@Test
	public void t() {
		Assume.assumeTrue(Files.exists(mysqlUtil.getDescriptionFile(demoBox)));
		MyCnfFileLikeHolder mcf = new MyCnfFileLikeHolder(demoBox.getMysqlInstance().getMycnfContent());
		
	}

}
