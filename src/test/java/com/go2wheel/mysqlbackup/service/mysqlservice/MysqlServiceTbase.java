package com.go2wheel.mysqlbackup.service.mysqlservice;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.commands.MysqlService;
import com.go2wheel.mysqlbackup.exception.AppNotStartedException;
import com.go2wheel.mysqlbackup.exception.CommandNotFoundException;
import com.go2wheel.mysqlbackup.exception.MysqlAccessDeniedException;
import com.go2wheel.mysqlbackup.exception.UnExpectedOutputException;
import com.go2wheel.mysqlbackup.exception.UnExpectedInputException;
import com.go2wheel.mysqlbackup.installer.MySqlInstaller;
import com.go2wheel.mysqlbackup.installer.MysqlInstallInfo;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.Software;
import com.go2wheel.mysqlbackup.util.FileUtil;
import com.go2wheel.mysqlbackup.util.MysqlUtil;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class MysqlServiceTbase extends SpringBaseFort {

	@Autowired
	protected MysqlService mysqlService;

	@Autowired
	protected MysqlUtil mysqlUtil;

	@Autowired
	protected MySqlInstaller mySqlInstaller;

	protected Software software;
	

	protected void installMysql() throws JSchException, SchedulerException, IOException, UnExpectedOutputException,
			MysqlAccessDeniedException, AppNotStartedException, UnExpectedInputException, CommandNotFoundException {
		createSession();
		createMysqlIntance();
		installMysql(session, server, "123456");
	}

	protected void installMysql(Session session, Server server, String initPassword) throws JSchException,
			SchedulerException, IOException, UnExpectedOutputException, MysqlAccessDeniedException, AppNotStartedException, UnExpectedInputException, CommandNotFoundException {
		deleteAllJobs();
		mySqlInstaller.syncToDb();
		List<Software> sfs = softwareDbService.findByName("MYSQL");
		software = sfs.get(0);
		MysqlInstallInfo ii = (MysqlInstallInfo) mySqlInstaller.install(session, server, software, initPassword)
				.getResult();
		assertTrue(ii.isInstalled());
		mysqlService.enableLogbin(session, server);
	}

	protected void uninstall() throws JSchException, MysqlAccessDeniedException, AppNotStartedException, UnExpectedInputException {
		createSession();
		createMysqlIntance();
		mySqlInstaller.syncToDb();
		FacadeResult<MysqlInstallInfo> info = mySqlInstaller.unInstall(session, server, software);
		assertFalse(info.getResult().isInstalled());
	}

	protected void uninstall(Session session, Server server) throws JSchException, MysqlAccessDeniedException, AppNotStartedException, UnExpectedInputException {
		try {
			createMysqlIntance(server, "");
			mySqlInstaller.syncToDb();
		} catch (Exception e) {
		}
		FacadeResult<MysqlInstallInfo> info = mySqlInstaller.unInstall(session, server, software);
		assertFalse(info.getResult().isInstalled());
	}
	
	protected void clearDumpsFolder() throws IOException {
		Path p = settingsIndb.getDumpsDir(server);
		Files.list(p).forEach(f -> {
			try {
				FileUtil.deleteFolder(f, false);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

}
