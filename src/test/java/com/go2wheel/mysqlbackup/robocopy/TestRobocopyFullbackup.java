package com.go2wheel.mysqlbackup.robocopy;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.borg.RobocopyService;
import com.go2wheel.mysqlbackup.borg.RobocopyService.SSHPowershellInvokeResult;
import com.go2wheel.mysqlbackup.exception.CommandNotFoundException;
import com.go2wheel.mysqlbackup.exception.UnExpectedContentException;
import com.go2wheel.mysqlbackup.model.RobocopyDescription;
import com.go2wheel.mysqlbackup.model.RobocopyItem;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.google.common.collect.Lists;
import com.jcraft.jsch.JSchException;

public class TestRobocopyFullbackup extends SpringBaseFort {
	
    @Rule
    public TemporaryFolder repofolder= new TemporaryFolder();
    
    @Rule
    public TemporaryFolder srcfolder= new TemporaryFolder();
    
    @Autowired
    private RobocopyService robocopyService;
    
    @Value("${myapp.app.archive}")
    private String zipApp;

	private Path createDemoSrc() throws IOException {
		Path rt = srcfolder.getRoot().toPath();
		createALocalFile(rt.resolve("a/afile.txt"), "abc");
		return rt;
	}
	
	private RobocopyDescription grpd() throws IOException {
		RobocopyDescription rd = new RobocopyDescription.RobocopyDescriptionBuilder(server.getId(), repofolder.getRoot().toPath().toAbsolutePath().toString()).build();
		String compress = String.format("& '%s' a -ms %%s %%s", zipApp);
		rd.setCompressCommand(compress);
		String expand = String.format("& '%s' x -o+ %%s %%s", zipApp);
		rd.setExpandCommand(expand);
		rd.setArchiveName("hello.rar");
		//	& "C:\Program Files\WinRAR\Rar.exe" x -o+ upload  .\\upload ALWAYS TREAT extract destination as a folder.
		// -ms
//        If <list> is not specified, -ms switch will use the default
//        set of extensions, which includes the following file types:
//
//        7z, ace, arj, bz2, cab, gz, jpeg, jpg, lha, lz, lzh, mp3,
//        rar, taz, tgz, xz, z, zip, zipx
		RobocopyItem ri = new RobocopyItem(0, srcfolder.getRoot().toPath().toAbsolutePath().toString(), "abc");
		RobocopyItem ri1 = new RobocopyItem(0, srcfolder.getRoot().toPath().toAbsolutePath().toString(), "abc1");
		rd.setRobocopyItems(Lists.newArrayList(ri, ri1));
		Files.createDirectories(Paths.get(rd.getWorkingSpaceAbsolute()));
		return rd;
	}
	
	@Test
	public void tjsonfy() throws JSchException, SchedulerException, IOException {
		createSessionLocalHostWindowsAfterClear();
		RobocopyDescription robocopyDescription = grpd();
		
		String json = objectMapper.writeValueAsString(robocopyDescription);
		
		json = StringUtil.quotation(json, false);
		
		String[] names = applicationContext.getBeanNamesForType(ObjectMapper.class);
		assertThat(names.length, equalTo(1));
		
		List<String> lines = StringUtil.splitLines(json);
		assertThat(lines.size(), equalTo(1));
		
		
	}
	
	@Test
	public void tFullback() throws IOException, JSchException, SchedulerException, NoSuchAlgorithmException, UnExpectedContentException, CommandNotFoundException {
		createSessionLocalHostWindowsAfterClear();
		createDemoSrc();
		RobocopyDescription robocopyDescription = grpd();
		
		Path workingCompressed = Paths.get(robocopyDescription.getWorkingSpaceCompressed());
		Files.createDirectories(workingCompressed);
		boolean b = robocopyService.fullBackup(session, server, robocopyDescription, robocopyDescription.getRobocopyItems());
		assertTrue(b);
		assertTrue(Files.exists(Paths.get(robocopyDescription.getWorkingSpaceScriptFile())));
		
		RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, robocopyDescription.getWorkingSpaceScriptFile() + " -action echo");
		assertThat(rcr.getExitValue(), equalTo(0));
		
		assertThat(rcr.getAllTrimedNotEmptyLines().get(0), equalTo("echo"));
	}

	
	@Test
	public void tFullBackupStep() throws JSchException, IOException, SchedulerException, CommandNotFoundException, NoSuchAlgorithmException, UnExpectedContentException {
		createSessionLocalHostWindowsAfterClear();
		createDemoSrc();
		RobocopyDescription robocopyDescription = grpd();
		

		
		List<String> scripts = robocopyService.getBackupScripts(session, server, robocopyDescription, robocopyDescription.getRobocopyItems());
		String s = scripts.stream().collect(Collectors.joining(";"));
		assertThat(scripts.size(), equalTo(2));
		
		
		FacadeResult<List<SSHPowershellInvokeResult>> frr = robocopyService.executeRobocopies(session, server, robocopyDescription, robocopyDescription.getRobocopyItems());
		assertThat(frr.getResult().size(), equalTo(2));
		assertThat(frr.getResult().iterator().next().exitCode(), equalTo(1));
		
		Path dstPath = Paths.get(robocopyDescription.getRobocopyItemDst("abc"));
		Path afile = dstPath.resolve("a/afile.txt");
		assertTrue(Files.exists(dstPath));
		assertTrue(Files.exists(afile));

		s= new String(Files.readAllBytes(afile));
		assertThat(s, equalTo("abc"));
		
		/**
		 * compress the robocopydst folder in repofolder to the compressed folder in repofolder.
		 */
		SSHPowershellInvokeResult sshpir = robocopyService.compressArchive(session, server, robocopyDescription);
		assertThat(sshpir.exitCode(), equalTo(9)); // when compress dst folder does'nt exists.
		
		Path workingCompressed = Paths.get(robocopyDescription.getWorkingSpaceCompressed());
		Files.createDirectories(workingCompressed);
		
		sshpir = robocopyService.compressArchive(session, server, robocopyDescription);
		assertThat(sshpir.exitCode(), equalTo(0));
		
		Path zipPath = Paths.get(robocopyDescription.getWorkingSpaceCompressedArchive()); 
		assertTrue(Files.exists(zipPath));
		
		Path extractPath = Paths.get(robocopyDescription.getWorkingSpaceExpanded());
		
		sshpir = robocopyService.expandArchive(session, server, robocopyDescription, zipPath.toAbsolutePath().toString(), extractPath.toAbsolutePath().toString());
		assertThat(sshpir.exitCode(), equalTo(0));
		
		String rp = robocopyDescription.getRobocopyDstNoRoot();
		
		// abc folder is one robocopyitem's dst.
		afile = extractPath.resolve(rp).resolve("abc").resolve("a").resolve("afile.txt");
		
		assertTrue(Files.exists(afile));
		
		FacadeResult<Path> p = robocopyService.downloadCompressed(session, server, robocopyDescription);
		assertTrue(Files.exists(p.getResult()));
		assertThat(p.getResult().getFileName().toString(), equalTo(robocopyDescription.getArchiveName()));
		p = robocopyService.downloadCompressed(session, server, robocopyDescription); // download again.
		assertThat(Files.list(settingsIndb.getRepoDir(server)).count(), equalTo(1L));
	}
}
