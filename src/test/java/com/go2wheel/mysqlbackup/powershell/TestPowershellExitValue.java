package com.go2wheel.mysqlbackup.powershell;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.quartz.SchedulerException;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.jcraft.jsch.JSchException;

public class TestPowershellExitValue  extends SpringBaseFort {
	
    @Rule
    public TemporaryFolder srcfolder= new TemporaryFolder();
	
	private Path createDemoSrc() throws IOException {
		Path rt = srcfolder.getRoot().toPath();
		createALocalFile(rt.resolve("a/afile.txt"), "abc");
		return rt;
	}
	
	@Test
	public void tGetItem() throws JSchException, SchedulerException, IOException {
		creates();
		String cmd = String.format("Get-Item -Path notexists");
		RemoteCommandResult rr = SSHcommonUtil.runRemoteCommand(session, cmd);
		assertThat(rr.getExitValue(), equalTo(1));
		
		cmd = String.format("Get-Item -Path %s", srcfolder.getRoot().getAbsolutePath().toString());
		rr = SSHcommonUtil.runRemoteCommand(session, cmd);
		assertThat(rr.getExitValue(), equalTo(0));
	}
	
	private void creates() throws JSchException, SchedulerException {
		clearDb();
		createSessionLocalHostWindows();
		deleteAllJobs();
	}
}