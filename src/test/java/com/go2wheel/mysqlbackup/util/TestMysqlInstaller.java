package com.go2wheel.mysqlbackup.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.mysqlinstaller.MySqlInstaller;
import com.go2wheel.mysqlbackup.util.MysqlUtil.MysqlInstallInfo;
import com.go2wheel.mysqlbackup.value.FacadeResult;

public class TestMysqlInstaller extends SpringBaseFort {
	
	@Autowired
	private MySqlInstaller mii;
	
	
	@Test
	public void tInstall() {
		FacadeResult<MysqlInstallInfo> info = mii.install(session, box, "56", "123456");
		assertTrue(info.getResult().isInstalled());
	}
	
}
