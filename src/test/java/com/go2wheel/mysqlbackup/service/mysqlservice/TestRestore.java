package com.go2wheel.mysqlbackup.service.mysqlservice;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.ServerDataCleanerRule;
import com.go2wheel.mysqlbackup.exception.AppNotStartedException;
import com.go2wheel.mysqlbackup.exception.MysqlAccessDeniedException;
import com.go2wheel.mysqlbackup.exception.UnExpectedContentException;
import com.go2wheel.mysqlbackup.exception.UnExpectedInputException;
import com.go2wheel.mysqlbackup.model.MysqlInstance;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.util.RemotePathUtil;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.value.LinuxLsl;
import com.go2wheel.mysqlbackup.value.MysqlDumpFolder;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class TestRestore extends MysqlServiceTbase {

	
	@Rule
	@Autowired
	public ServerDataCleanerRule sdc; 
	

	@Test
	public void testMysqldump()
			throws JSchException, IOException, MysqlAccessDeniedException, AppNotStartedException, NoSuchAlgorithmException, UnExpectedInputException, UnExpectedContentException, SchedulerException {
		sdc.setHost(HOST_DEFAULT_GET);
		clearDb();
		
		//init get
		installMysql();
		mysqlService.mysqlDump(session, server);
		
		//init set
		Server targetServer = createServer(HOST_DEFAULT_SET, true);
		createMysqlIntance(targetServer, "654321");
		Session targetSession = createSession(targetServer);
		installMysql(targetSession, targetServer, "654321");
		
		List<MysqlDumpFolder> mss = mysqlService.listDumpFolders(server);
		MysqlDumpFolder mdf = mss.get(0);
		String remoteFolder = mysqlService.uploadDumpFolder(server, targetServer, targetSession, mdf.getFolder());
		
		assertTrue(SSHcommonUtil.fileExists(targetSession, remoteFolder));
		List<LinuxLsl> files = SSHcommonUtil.listRemoteFiles(targetSession, remoteFolder);
		
		assertThat(files.size(), equalTo(1));
		assertThat("uploaded dump sql are equal", (long)files.size(), equalTo(Files.list(mdf.getFolder()).count()));
		
		String dumpfn = RemotePathUtil.getFileName(server.getMysqlInstance().getDumpFileName());
		
		dumpfn = RemotePathUtil.join(remoteFolder, dumpfn);
		
		MysqlInstance sourceMysqlInstance = server.getMysqlInstance();
		MysqlInstance targetMysqlInstance = targetServer.getMysqlInstance();
		
		mysqlService.importDumped(targetSession, targetServer, sourceMysqlInstance, targetMysqlInstance, remoteFolder);
		
	}

}
