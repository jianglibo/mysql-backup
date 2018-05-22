package com.go2wheel.mysqlbackup.util;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.quartz.SchedulerException;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.mysqlinstaller.MySqlInstaller;
import com.go2wheel.mysqlbackup.util.MysqlUtil.MysqlInstallInfo;
import com.go2wheel.mysqlbackup.value.FacadeResult;

public class TestMysqlInstaller extends SpringBaseFort {
	
	private MysqlUtil mysqlUtil;
	private MySqlInstaller mii = new MySqlInstaller();
	
	@Before
	public void b() throws IOException, SchedulerException {
		super.before();
		mysqlUtil = new MysqlUtil();
		mysqlUtil.setAppSettings(appSettings);
		
		mii = new MySqlInstaller();
		mii.setFileDownloader(fileDownloader);
		mii.setMysqlUtil(mysqlUtil);
	}
	
	@Test
	public void tInstall() {
		FacadeResult<MysqlInstallInfo> info = mii.install(session, box, "56", "123456");
		assertTrue(info.getResult().isInstalled());
	}
	
	
//	@Test
//	public void tUninstallMysql() {
//		FacadeResult<MysqlInstallInfo> info = mii.unInstall(session, box);
//		assertFalse(info.getResult().isInstalled());
//	}

}
