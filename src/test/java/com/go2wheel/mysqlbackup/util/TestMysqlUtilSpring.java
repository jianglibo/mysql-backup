package com.go2wheel.mysqlbackup.util;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.exception.MysqlAccessDeniedException;
import com.go2wheel.mysqlbackup.exception.MysqlNotStartedException;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.exception.ScpException;
import com.go2wheel.mysqlbackup.installer.MysqlInstallInfo;
import com.go2wheel.mysqlbackup.model.MysqlInstance;
import com.go2wheel.mysqlbackup.value.BackupedFiles;
import com.go2wheel.mysqlbackup.value.ConfigValue;
import com.go2wheel.mysqlbackup.value.LogBinSetting;
import com.go2wheel.mysqlbackup.value.MycnfFileHolder;
import com.jcraft.jsch.JSchException;

public class TestMysqlUtilSpring extends SpringBaseFort {

	@Autowired
	private MysqlUtil mysqlUtil;
	
	@Before
	public void be() {
		clearDb();
		createSession();
		createMysqlIntance();
	}


	@Test
	public void testMysqlVariable()
			throws JSchException, IOException, MysqlAccessDeniedException, MysqlNotStartedException {
		LogBinSetting lbs = mysqlUtil.getLogbinState(session, server);
		assertThat(lbs.getMap().size(), greaterThan(3));
	}

	@Test
	public void testEnableLogBinOption() throws IOException, JSchException, ScpException, RunRemoteCommandException {
		MycnfFileHolder mfh = mysqlUtil.getMyCnfFile(session, server);
		mfh.enableBinLog();
		ConfigValue cv = mfh.getConfigValue(MycnfFileHolder.MYSQLD_LOG_BIN_KEY);
		assertThat(cv.getValue(), equalTo(MycnfFileHolder.DEFAULT_LOG_BIN_BASE_NAME));
		assertTrue("mycnf field should be set.",
				server.getMysqlInstance().getMycnfFile() != null && server.getMysqlInstance().getMycnfFile().length() > 3);

		String remoteFile = server.getMysqlInstance().getMycnfFile();

		BackupedFiles backupedFiles = SSHcommonUtil.getRemoteBackupedFiles(session, remoteFile);
		int flsi = backupedFiles.getBackups().size();
		
		SSHcommonUtil.backupFile(session, server.getMysqlInstance().getMycnfFile());
		byte[] bytes = String.join("\n", mfh.getLines()).getBytes();
		ScpUtil.to(session, server.getMysqlInstance().getMycnfFile(), bytes);

		String s = ScpUtil.from(session, server.getMysqlInstance().getMycnfFile()).toString();
		mfh = new MycnfFileHolder(new ArrayList<>(StringUtil.splitLines(s)));
		cv = mfh.getConfigValue(MycnfFileHolder.MYSQLD_LOG_BIN_KEY);
		assertThat(cv.getValue(), equalTo(MycnfFileHolder.DEFAULT_LOG_BIN_BASE_NAME));

		backupedFiles = SSHcommonUtil.getRemoteBackupedFiles(session, remoteFile);
		assertThat(backupedFiles.getBackups().size(), equalTo(flsi + 1));

		SSHcommonUtil.deleteBackupedFiles(session, remoteFile);
		backupedFiles = SSHcommonUtil.getRemoteBackupedFiles(session, remoteFile);
		assertThat(backupedFiles.getBackups().size(), equalTo(1));
	}

	@Test
	public void testMycnf() throws RunRemoteCommandException, IOException, JSchException, ScpException {
		String s = mysqlUtil.getEffectiveMyCnf(session, server);
		assertThat(s, equalTo("/etc/my.cnf"));

		MycnfFileHolder mfh = mysqlUtil.getMyCnfFile(session, server);
		assertTrue(mfh.getLines().size() > 0);
	}

	@Test
	public void testVariables()
			throws JSchException, IOException, MysqlAccessDeniedException, MysqlNotStartedException {
		Map<String, String> map = mysqlUtil.getVariables(session, server.getMysqlInstance().getUsername("root"),
				server.getMysqlInstance().getPassword(), MysqlInstance.VAR_DATADIR);
		assertTrue("contains datadir", map.containsKey(MysqlInstance.VAR_DATADIR));
	}

	@Test
	public void mysqlInof() throws RunRemoteCommandException, JSchException, IOException, MysqlAccessDeniedException {
		MysqlInstallInfo info = mysqlUtil.getInstallInfo(session, server);
		System.out.println(info);
	}
}
