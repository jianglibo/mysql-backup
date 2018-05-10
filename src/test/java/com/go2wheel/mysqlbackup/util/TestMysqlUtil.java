package com.go2wheel.mysqlbackup.util;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.exception.ScpException;
import com.go2wheel.mysqlbackup.jsch.SshBaseFort;
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
		LogBinSetting lbs = mysqlUtil.getLogbinState(session, box);
		assertThat(lbs.getMap().size(), equalTo(3));
	}
	
	@Test
	public void testMysqlDump() throws JSchException, IOException {
//		DumpResult dumpResult = mysqlUtil.mysqldump(session, box);
//		assertNotNull(dumpResult);
	}
	
	@Test
	public void testEnableLogBinOption() throws IOException, JSchException, ScpException, RunRemoteCommandException {
		MycnfFileHolder mfh = mysqlUtil.getMyCnfFile(session, box);
		mfh.enableBinLog();
		ConfigValue cv = mfh.getConfigValue(LogBinSetting.LOG_BIN_VARIABLE);
		assertThat(cv.getValue(), equalTo(MycnfFileHolder.DEFAULT_LOG_BIN_BASE_NAME));
		assertTrue("mycnf field should be set.", box.getMysqlInstance().getMycnfFile() != null && box.getMysqlInstance().getMycnfFile().length() > 3);
		
		String remoteFile = box.getMysqlInstance().getMycnfFile();
		
		BackupedFiles backupedFiles = SSHcommonUtil.getRemoteBackupedFiles(session, remoteFile);
		int flsi = backupedFiles.getBackups().size();
		SSHcommonUtil.backupFile(session, box.getMysqlInstance().getMycnfFile());
		byte[] bytes = String.join("\n",mfh.getLines()).getBytes();
		ScpUtil.to(session, box.getMysqlInstance().getMycnfFile(), bytes);
		
		String s = ScpUtil.from(session, box.getMysqlInstance().getMycnfFile()).toString();
		mfh = new MycnfFileHolder(new ArrayList<>(StringUtil.splitLines(s)));
		cv = mfh.getConfigValue(LogBinSetting.LOG_BIN_VARIABLE);
		assertThat(cv.getValue(), equalTo(MycnfFileHolder.DEFAULT_LOG_BIN_BASE_NAME));

		backupedFiles = SSHcommonUtil.getRemoteBackupedFiles(session, remoteFile);
		assertThat(backupedFiles.getBackups().size(), equalTo(flsi + 1));
		
		SSHcommonUtil.deleteBackupedFiles(session, remoteFile);
		backupedFiles = SSHcommonUtil.getRemoteBackupedFiles(session, remoteFile);
		assertThat(backupedFiles.getBackups().size(), equalTo(1));

	}
	
	@Test
	public void testMycnf() throws RunRemoteCommandException, IOException, JSchException, ScpException {
		String s = mysqlUtil.getEffectiveMyCnf(session, box);
		assertThat(s, equalTo("/etc/my.cnf"));
		
		MycnfFileHolder mfh = mysqlUtil.getMyCnfFile(session, box);
		assertTrue(mfh.getLines().size() > 0);
	}

}
