package com.go2wheel.mysqlbackup.robocopy;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.service.RobocopyService;
import com.go2wheel.mysqlbackup.util.ProcessExecUtil;

public class TestRoboCopyExitValue extends SpringBaseFort {
	
    @Rule
    public TemporaryFolder tfolder= new TemporaryFolder();
    
    @Rule
    public TemporaryFolder srcfolder= new TemporaryFolder();
    
    @Autowired
    private RobocopyService robocopyService;

    
//    private Path robocopysrc = Paths.get("fixtures", "robocopysrc");

//    Robocopy.exe .\elm-workspace\ .\elm-workspace-copy  /e /xd log log1
//    0	No files were copied. No failure was encountered. No files were mismatched. The files already exist in the destination directory; therefore, the copy operation was skipped.
//    1	All files were copied successfully.
//    2	There are some additional files in the destination directory that are not present in the source directory. No files were copied.
//    3	Some files were copied. Additional files were present. No failure was encountered.
//    5	Some files were copied. Some files were mismatched. No failure was encountered.
//    6	Additional files and mismatched files exist. No files were copied and no failures were encountered. This means that the files already exist in the destination directory.
//    7	Files were copied, a file mismatch was present, and additional files were present.
//    8	Several files did not copy.
    
	@Autowired
	private ProcessExecUtil peu;
	
	private Path createDemoSrc() throws IOException {
		Path rt = srcfolder.getRoot().toPath();
		createALocalFile(rt.resolve("a/afile.txt"), "abc");
		return rt;
	}
	
	@Test
	public void tNoTarget() throws SchedulerException, IOException {
		Path dst = tfolder.getRoot().toPath();
		String cmd = String.format("Robocopy.exe %s;$LASTEXITCODE", dst.toAbsolutePath().toString());
//		SSHPowershellInvokeResult rr = robocopyService.invokeRoboCopyCommand(session, cmd);
//		assertThat(rr.exitCode(), equalTo(16));
	}
	
	@Test
	public void tNormalCopy() throws IOException, InterruptedException, SchedulerException{
		Path dst = tfolder.getRoot().toPath();
		// this command return only the lines of the files changed or created.
//		Robocopy.exe %s %s /xd log log1 /e /bytes /fp /njh /njs |ForEach-Object {$_.trim()} |Where-Object {$_ -notmatch '.*\\$'} | Where-Object {($_ -split  '\s+').length -gt 2}
		Path src = createDemoSrc();
		
		String cmd = String.format("Robocopy.exe %s %s /xd log log1 /e /bytes /fp /njh /njs |ForEach-Object {$_.trim()} |Where-Object {$_ -notmatch '.*\\\\$'} | Where-Object {($_ -split  '\\s+').length -gt 2}", src.toAbsolutePath().toString(), dst.toAbsolutePath().toString());
//		SSHPowershellInvokeResult rr = robocopyService.invokeRoboCopyCommand(session, cmd);
//		assertThat(rr.exitCode(), equalTo(1));
//		rr = robocopyService.invokeRoboCopyCommand(session, cmd);
//		assertThat(rr.exitCode(), equalTo(0));
	}
	
	@Test
	public void tSshNoFile() throws IOException, InterruptedException, SchedulerException{
		Path dst = tfolder.getRoot().toPath();
		// this command return only the lines of the files changed or created.
//		Robocopy.exe %s %s /xd log log1 /e /bytes /fp /njh /njs |ForEach-Object {$_.trim()} |Where-Object {$_ -notmatch '.*\\$'} | Where-Object {($_ -split  '\s+').length -gt 2}
		Path src = createDemoSrc();
		String cmd = String.format("Robocopy.exe %s %s *.kkk /xd log log1 /bytes /fp /njh /njs |ForEach-Object {$_.trim()} |Where-Object {$_ -notmatch '.*\\\\$'} | Where-Object {($_ -split  '\\s+').length -gt 2}", src.toAbsolutePath().toString(), dst.toAbsolutePath().toString());
//		RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, "GBK", cmd);
//		assertThat(rcr.getExitValue(), equalTo(0));
	}
	
	private void creates() throws SchedulerException {
		clearDb();
		deleteAllJobs();
	}
}
