package com.go2wheel.mysqlbackup.util;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.exception.MysqlAccessDeniedException;
import com.go2wheel.mysqlbackup.exception.MysqlNotStartedException;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.exception.ScpException;
import com.go2wheel.mysqlbackup.util.MysqlUtil.MysqlInstallInfo;
import com.go2wheel.mysqlbackup.value.BackupedFiles;
import com.go2wheel.mysqlbackup.value.ConfigValue;
import com.go2wheel.mysqlbackup.value.LogBinSetting;
import com.go2wheel.mysqlbackup.value.MycnfFileHolder;
import com.go2wheel.mysqlbackup.value.MysqlInstanceYml;
import com.jcraft.jsch.JSchException;

public class TestMysqlUtilSpring extends SpringBaseFort {

	@Autowired
	private MysqlUtil mysqlUtil;


	@Test
	public void testMysqlVariable()
			throws JSchException, IOException, MysqlAccessDeniedException, MysqlNotStartedException {
		LogBinSetting lbs = mysqlUtil.getLogbinState(session, box);
		assertThat(lbs.getMap().size(), equalTo(3));
	}

	@Test
	public void testEnableLogBinOption() throws IOException, JSchException, ScpException, RunRemoteCommandException {
		MycnfFileHolder mfh = mysqlUtil.getMyCnfFile(session, box);
		mfh.enableBinLog();
		ConfigValue cv = mfh.getConfigValue(MycnfFileHolder.MYSQLD_LOG_BIN_KEY);
		assertThat(cv.getValue(), equalTo(MycnfFileHolder.DEFAULT_LOG_BIN_BASE_NAME));
		assertTrue("mycnf field should be set.",
				box.getMysqlInstance().getMycnfFile() != null && box.getMysqlInstance().getMycnfFile().length() > 3);

		String remoteFile = box.getMysqlInstance().getMycnfFile();

		BackupedFiles backupedFiles = SSHcommonUtil.getRemoteBackupedFiles(session, remoteFile);
		int flsi = backupedFiles.getBackups().size();
		SSHcommonUtil.backupFile(session, box.getMysqlInstance().getMycnfFile());
		byte[] bytes = String.join("\n", mfh.getLines()).getBytes();
		ScpUtil.to(session, box.getMysqlInstance().getMycnfFile(), bytes);

		String s = ScpUtil.from(session, box.getMysqlInstance().getMycnfFile()).toString();
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
		String s = mysqlUtil.getEffectiveMyCnf(session, box);
		assertThat(s, equalTo("/etc/my.cnf"));

		MycnfFileHolder mfh = mysqlUtil.getMyCnfFile(session, box);
		assertTrue(mfh.getLines().size() > 0);
	}

	@Test
	public void testVariables()
			throws JSchException, IOException, MysqlAccessDeniedException, MysqlNotStartedException {
		Map<String, String> map = mysqlUtil.getVariables(session, box.getMysqlInstance().getUsername("root"),
				box.getMysqlInstance().getPassword(), MysqlInstanceYml.VAR_DATADIR);
		assertTrue("contains datadir", map.containsKey(MysqlInstanceYml.VAR_DATADIR));
	}

	@Test
	public void mysqlInof() throws RunRemoteCommandException, JSchException, IOException, MysqlAccessDeniedException {
		MysqlInstallInfo info = mysqlUtil.getInstallInfo(session, box);
		System.out.println(info);
	}
}
