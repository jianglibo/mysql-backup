package com.go2wheel.mysqlbackup.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.installer.MySqlInstaller;
import com.go2wheel.mysqlbackup.service.GlobalStore;
import com.go2wheel.mysqlbackup.util.MysqlUtil.MysqlInstallInfo;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.google.common.collect.Maps;

public class TestMysqlInstaller extends SpringBaseFort {
	
	@Autowired
	private GlobalStore globalStore;
	
	@Autowired
	private MySqlInstaller mii;
	
//	@Test
//	public void tInstall() {
//		clearDb();
//		createSession();
//		createMysqlIntance();
//		FacadeResult<MysqlInstallInfo> info = mii.install(session, server, "56", "123456");
//		assertTrue(info.getResult().isInstalled());
//	}
	
	@Test
	public void testAsync() {
		clearDb();
		createSession();
		createMysqlIntance();
		Map<String, String> parasMap = Maps.newHashMap();
		parasMap.put("version", "56");
		parasMap.put("initPassword", "123456");
		CompletableFuture<FacadeResult<MysqlInstallInfo>> cfm = mii.installAsync(server, parasMap);
		
		Thread t = Thread.currentThread();
		
		assertFalse(cfm.isDone());
		
		cfm.thenAccept(fr -> {
			System.out.print(fr);
			assertFalse(cfm.isCancelled());
			assertFalse(cfm.isCompletedExceptionally());
			assertTrue(cfm.isDone());
			t.interrupt();
		});
		
		try {
			Thread.sleep(200000);
		} catch (Exception e) {
		}
	}
	
}
