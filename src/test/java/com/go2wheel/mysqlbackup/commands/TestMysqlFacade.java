package com.go2wheel.mysqlbackup.commands;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

import com.go2wheel.mysqlbackup.jsch.SshBaseFort;
import com.go2wheel.mysqlbackup.util.MysqlUtil;
import com.go2wheel.mysqlbackup.value.MysqlDumpResult;
import com.jcraft.jsch.JSchException;

public class TestMysqlFacade extends SshBaseFort {
	
	private MysqlUtil mysqlUtil;
	
	private MysqlTaskFacade mysqlTaskFacade;
	
	@Before
	public void before() throws IOException {
		super.before();
		mysqlUtil = new MysqlUtil();
		mysqlUtil.setAppSettings(appSettings);
		mysqlTaskFacade = new MysqlTaskFacade();
		mysqlTaskFacade.setMysqlUtil(mysqlUtil);
	}

	@Test
	public void tDump() throws JSchException, IOException {
		Path localDumpFile = mysqlUtil.getDumpDir(box).resolve(Paths.get(MysqlUtil.DUMP_FILE_NAME).getFileName());
		if (Files.exists(localDumpFile)) {
			Files.delete(localDumpFile);
		}
		MysqlDumpResult mdr = mysqlTaskFacade.mysqlDump(session, box);
		assertTrue(mdr.isSuccess());
		
		mdr = mysqlTaskFacade.mysqlDump(session, box);
		assertFalse(mdr.isSuccess());
	}
	
	@Test
	public void tDownloadLogbin() {
		mysqlTaskFacade.downloadBinLog(session, box);
		mysqlTaskFacade.downloadBinLog(session, box);
	}

}
