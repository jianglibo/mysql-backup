package com.go2wheel.mysqlbackup.robocopy;

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

public class TestRoboCopyAppendOutput extends SpringBaseFort {
	
    @Rule
    public TemporaryFolder tfolder= new TemporaryFolder();
    
    @Rule
    public TemporaryFolder srcfolder= new TemporaryFolder();

	private Path createDemoSrc() throws IOException {
		Path rt = srcfolder.getRoot().toPath();
		createALocalFile(rt.resolve("a/afile.txt"), "abc");
		return rt;
	}
	
	@Test
	public void tSsh() throws IOException, InterruptedException, SchedulerException, JSchException {
		creates();
		Path dst = tfolder.getRoot().toPath();
		Path src = createDemoSrc();
		String cmd = String.format("Robocopy.exe %s %s /xd log log1 /e /bytes /fp /njh /njs |", src.toAbsolutePath().toString(), dst.toAbsolutePath().toString());
		RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, "GBK", cmd);
		assertThat(rcr.getExitValue(), equalTo(1));
	}
	
	@Test
	public void tAppendEnd() throws SchedulerException, JSchException, IOException {
		creates();
		String cmd = "1,2| ForEach-Object -End {3} -Process {$_}";
		RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, "GBK", cmd);
		assertThat(rcr.getAllTrimedNotEmptyLines().size(), equalTo(3));
		
		cmd = "1,2| ForEach-Object -End {$kk} -Process { if($_ -eq 1) {$kk=99;$_} else { $_} }";
		rcr = SSHcommonUtil.runRemoteCommand(session, "GBK", cmd);
		assertThat(rcr.getAllTrimedNotEmptyLines().size(), equalTo(3));
		assertThat(rcr.getAllTrimedNotEmptyLines().get(2), equalTo("99"));
	}
	
	private void creates() throws JSchException, SchedulerException {
		clearDb();
		createSessionLocalHostWindows();
		deleteAllJobs();
	}
	

}
