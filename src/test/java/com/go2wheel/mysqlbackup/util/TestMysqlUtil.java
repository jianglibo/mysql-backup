package com.go2wheel.mysqlbackup.util;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Map;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import com.go2wheel.mysqlbackup.jsch.SshBaseFort;
import com.go2wheel.mysqlbackup.util.MysqlDumpExpect.DumpResult;
import com.go2wheel.mysqlbackup.value.BackupedFiles;
import com.go2wheel.mysqlbackup.value.ConfigValue;
import com.go2wheel.mysqlbackup.value.LogBinSetting;
import com.go2wheel.mysqlbackup.value.MycnfFileHolder;
import com.jcraft.jsch.JSchException;

public class TestMysqlUtil extends SshBaseFort {
	
	private MysqlUtil mysqlUtil;
	
	@Before
	public void before() throws IOException {
		super.before();
		mysqlUtil = new MysqlUtil();
		mysqlUtil.setAppSettings(appSettings);
	}
	
	@Test
	public void testMysqlVariable() throws JSchException, IOException {
		LogBinSetting lbs = mysqlUtil.getLogbinState(sshSession, demoBox);
		assertThat(lbs.getMap().size(), equalTo(3));
	}
	
	@Test
	public void testMysqlDump() throws JSchException, IOException {
		DumpResult dumpResult = mysqlUtil.mysqldump(sshSession, demoBox);
		assertNotNull(dumpResult);
	}
	
	@Test
	public void testEnableLogBinOption() throws IOException, JSchException {
		MycnfFileHolder mfh = mysqlUtil.enableLogBinOption(sshSession, demoBox, "");
		ConfigValue cv = mfh.getConfigValue(LogBinSetting.LOG_BIN);
		assertThat(cv.getValue(), equalTo(MycnfFileHolder.DEFAULT_LOG_BIN_BASE_NAME));
		assertTrue("mycnf field should be set.", demoBox.getMysqlInstance().getMycnfFile() != null && demoBox.getMysqlInstance().getMycnfFile().length() > 3);
		
		String remoteFile = demoBox.getMysqlInstance().getMycnfFile();
		
		BackupedFiles backupedFiles = SSHcommonUtil.getBackupedFiles(sshSession, remoteFile);
		int flsi = backupedFiles.getBackups().size();
		SSHcommonUtil.backupFile(sshSession, demoBox.getMysqlInstance().getMycnfFile());
		byte[] bytes = String.join("\n",mfh.getLines()).getBytes();
		ScpUtil.to(sshSession, demoBox.getMysqlInstance().getMycnfFile(), bytes);
		
		String s = ScpUtil.from(sshSession, demoBox.getMysqlInstance().getMycnfFile());
		mfh = new MycnfFileHolder(new ArrayList<>(StringUtil.splitLines(s)));
		cv = mfh.getConfigValue(LogBinSetting.LOG_BIN);
		assertThat(cv.getValue(), equalTo(MycnfFileHolder.DEFAULT_LOG_BIN_BASE_NAME));

		backupedFiles = SSHcommonUtil.getBackupedFiles(sshSession, remoteFile);
		assertThat(backupedFiles.getBackups().size(), equalTo(flsi + 1));
		
		SSHcommonUtil.deleteBackupedFiles(sshSession, remoteFile);
		backupedFiles = SSHcommonUtil.getBackupedFiles(sshSession, remoteFile);
		assertThat(backupedFiles.getBackups().size(), equalTo(1));

	}

	@Test
	public void t() {
		Assume.assumeTrue(Files.exists(mysqlUtil.getDescriptionFile(demoBox)));
		MycnfFileHolder mcf = new MycnfFileHolder(demoBox.getMysqlInstance().getMycnfContent());
		
	}

}
