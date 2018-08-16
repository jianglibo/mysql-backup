package com.go2wheel.mysqlbackup.util;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.exception.AppNotStartedException;
import com.go2wheel.mysqlbackup.exception.ExceptionWrapper;
import com.go2wheel.mysqlbackup.exception.MysqlAccessDeniedException;
import com.go2wheel.mysqlbackup.installer.MySqlInstaller;
import com.go2wheel.mysqlbackup.installer.MysqlInstallInfo;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.Software;
import com.go2wheel.mysqlbackup.value.AsyncTaskValue;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.value.FacadeResult.CommonActionResult;
import com.google.common.collect.Maps;
import com.jcraft.jsch.JSchException;

public class TestMysqlInstaller extends SpringBaseFort {

	@Autowired
	private MySqlInstaller mii;

	private Software software;
	
	private void install(Server server, Software software) throws JSchException {
		Map<String, String> parasMap = Maps.newHashMap();
		parasMap.put("version", "56");
		parasMap.put("initPassword", "123456");
		mii.syncToDb();
		FacadeResult<MysqlInstallInfo> info = mii.install(server, software, parasMap);
		assertTrue(info.getResult().isInstalled());
	}
	
	
	@Test
	public void testUninstall() throws JSchException, MysqlAccessDeniedException, AppNotStartedException {
		clearDb();
		createSession();
		createMysqlIntance();
		mii.syncToDb();
		install(server, software);
		FacadeResult<MysqlInstallInfo> info = mii.unInstall(session, server, software);
		assertFalse(info.getResult().isInstalled());
	}

	@Test
	public void testInstallAsync() throws JSchException {
		clearDb();
		createSession();
		createMysqlIntance();
		Map<String, String> parasMap = Maps.newHashMap();
		parasMap.put("version", "56");
		parasMap.put("initPassword", "123456");
		mii.syncToDb();
		software = softwareDbService.findByName("MYSQL").get(0);

		CompletableFuture<AsyncTaskValue> cfm = mii.installAsync(server, software, "abc", 1L, parasMap);

		Thread t = Thread.currentThread();

		assertFalse(cfm.isDone());

		cfm.thenAccept(fr -> {
			@SuppressWarnings("unchecked")
			FacadeResult<MysqlInstallInfo> fmi = (FacadeResult<MysqlInstallInfo>) fr.getResult();
			
			assertTrue(fmi.getResult().isInstalled());
			assertThat(fmi.getCommonActionResult(), equalTo(CommonActionResult.PREVIOUSLY_DONE));
			assertFalse(cfm.isCancelled());
			assertFalse(cfm.isCompletedExceptionally());
			assertTrue(cfm.isDone());
			t.interrupt();
		}).exceptionally(tr -> {
			t.interrupt();
			throw new ExceptionWrapper(tr);
		});

		try {
			Thread.sleep(200000);
		} catch (Exception e) {
		}
	}
}
