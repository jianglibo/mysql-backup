package com.go2wheel.mysqlbackup.robocopy;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.util.ProcessExecUtil;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.value.ProcessExecResult;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.jcraft.jsch.JSchException;

public class TestRoboCopySsh extends SpringBaseFort {
	
    @Rule
    public TemporaryFolder tfolder= new TemporaryFolder();
    
    @Rule
    public TemporaryFolder srcfolder= new TemporaryFolder();

    
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
	public void tLocal() throws IOException, InterruptedException {
		Path dst = tfolder.getRoot().toPath();
		
		Path src = createDemoSrc();
		ProcessExecResult per = peu.run(Arrays.asList("robocopy.exe", src.toString(), dst.toString(),"/unicode", "/e", "/xd", "log", "log1"));
		assertThat(per.getExitValue(), equalTo(1)); // all files were copied.
		assertTrue(per.getStdError().size() == 0);
		per.getStdOut().stream()
//		.map(line -> {
//			try {
//				byte[] bytes = line.getBytes();
//				return new String(bytes, "GBK");
//			} catch (UnsupportedEncodingException e) {
//				e.printStackTrace();
//				return "";
//			}
//		})
		.forEach(System.out::println);
	}
	
	//Set-Location C:\db\;Get-ChildItem -Recurse |Resolve-Path -Relative
	
	//Get-ChildItem c:\db -Recurse |Select-Object FullName | 
	// Get-ChildItem c:\db -Recurse |ForEach-Object {$_.FullName -replace '^c:\\db\\',''}
	
//	@Test
//	public void tCd() throws JSchException, IOException {
//		clearDb();
//		createSessionLocalHostWindows();
//		String cmd = "set-location";
//		RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, null, cmd);
//		assertThat(rcr.getAllTrimedNotEmptyLines().size(), equalTo(2));
//	}
	
	
	@Test
	public void tRunArrayBegin() throws JSchException, IOException {
		clearDb();
		createSessionLocalHostWindows();
		String cmd = "'a', 'b' | write-host";
		RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, null, cmd);
		assertThat(rcr.getAllTrimedNotEmptyLines().size(), equalTo(2));
	}
	
	@Test
	public void tHowlongCommand() throws JSchException, IOException {
		int num = 6500;
		String c = IntStream.range(0, num).mapToObj(i -> String.valueOf(i)).collect(Collectors.joining(","));
		c += " | Write-Output";
		clearDb();
		createSessionLocalHostWindows();
		int length = c.length();
		RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, "GBK", c);
		assertThat(rcr.getAllTrimedNotEmptyLines().size(), equalTo(num));
		assertThat(rcr.getAllTrimedNotEmptyLines().stream().collect(Collectors.joining(",")).length() + " | Write-Output".length(), equalTo(length));
	}
	
	@Test
	public void tSsh() throws IOException, InterruptedException, SchedulerException, JSchException {
		clearDb();
		createSessionLocalHostWindows();
		Path dst = tfolder.getRoot().toPath();
		deleteAllJobs();
		// this command return only the lines of the files changed or created.
//		Robocopy.exe %s %s /xd log log1 /e /bytes /fp /njh /njs |ForEach-Object {$_.trim()} |Where-Object {$_ -notmatch '.*\\$'} | Where-Object {($_ -split  '\s+').length -gt 2}
		Path src = createDemoSrc();
		String cmd = String.format("Robocopy.exe %s %s /xd log log1 /e /bytes /fp /njh /njs |ForEach-Object {$_.trim()} |Where-Object {$_ -notmatch '.*\\\\$'} | Where-Object {($_ -split  '\\s+').length -gt 2}", src.toAbsolutePath().toString(), dst.toAbsolutePath().toString());
		RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, "GBK", cmd);
		
		Path afile = dst.resolve("a").resolve("afile.txt");
		assertTrue(Files.exists(afile));
	}
	
	@Test
	public void tExitCode() throws JSchException, SchedulerException, IOException {
		clearDb();
		createSessionLocalHostWindows();
		Path dst = tfolder.getRoot().toPath();
		deleteAllJobs();
		String cmd = String.format("Robocopy1.exe %s", dst.toAbsolutePath().toString());
		RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, "GBK", "GBK", cmd);
		assertThat(rcr.getExitValue(), equalTo(0));
	}
	
	
	@Test
	public void tSshHasChineseFileName() throws IOException, InterruptedException, SchedulerException, JSchException {
		clearDb();
		createSessionLocalHostWindows();
		Path dst = tfolder.getRoot().toPath();
		deleteAllJobs();
		// this command return only the lines of the files changed or created.
//		Robocopy.exe %s %s /xd log log1 /e /bytes /fp /njh /njs |ForEach-Object {$_.trim()} |Where-Object {$_ -notmatch '.*\\$'} | Where-Object {($_ -split  '\s+').length -gt 2}
		
		Path src = srcfolder.getRoot().toPath();
		createALocalFile(src.resolve("中文目录/afile.txt"), "abc");
		
		String cmd = String.format("Robocopy.exe %s %s /xd log log1 /e /bytes /fp /njh /njs |ForEach-Object {$_.trim()} |Where-Object {$_ -notmatch '.*\\\\$'} | Where-Object {($_ -split  '\\s+').length -gt 2}", src.toAbsolutePath().toString(), dst.toAbsolutePath().toString());
		RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, "GBK", "GBK", cmd);
		
		Path afile = dst.resolve("中文目录").resolve("afile.txt");
		assertTrue(Files.exists(afile));
	}
	
//	'C:\Documents and Settings\User01\My Documents\My Pictures' | Split-Path
	
//	New-Item -Type Directory -Path e:\a\b\c\d  -ErrorAction Ignore
	
	

	
	/**
	 * In the windows server we just invoke robocopy normally. we can invoke archive command from backup server at any time, then download the archived file,  after that archive point,
	 *  we invoke robocopy again, but save the log and the changed files, then archive them and download.
	 *  we can start a new round at any time. 
	 *  
	 *  I must copy all changed file to a new place then compress-archive it.
	 */


}
