package com.go2wheel.mysqlbackup.robocopy;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.borg.RobocopyService;
import com.go2wheel.mysqlbackup.borg.RobocopyService.SSHPowershellInvokeResult;
import com.go2wheel.mysqlbackup.exception.CommandNotFoundException;
import com.go2wheel.mysqlbackup.model.RobocopyDescription;
import com.go2wheel.mysqlbackup.model.RobocopyItem;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.google.common.collect.Lists;
import com.jcraft.jsch.JSchException;

public class TestFullbackup extends SpringBaseFort {
	
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
	
	private RobocopyDescription grpd() {
		RobocopyDescription rd = new RobocopyDescription.RobocopyDescriptionBuilder(server.getId(), repofolder.getRoot().toPath().toAbsolutePath().toString()).build();
		String compress = String.format("& '%s' a %%s %%s", zipApp);
		rd.setCompressCommand(compress);
		String expand = String.format("& '%s' x -o+ %%s %%s", zipApp);
		rd.setExpandCommand(expand);
		//	& "C:\Program Files\WinRAR\Rar.exe" x -o+ upload  .\\upload ALWAYS TREAT extract destination as a folder.
		// -ms
		
//        If <list> is not specified, -ms switch will use the default
//        set of extensions, which includes the following file types:
//
//        7z, ace, arj, bz2, cab, gz, jpeg, jpg, lha, lz, lzh, mp3,
//        rar, taz, tgz, xz, z, zip, zipx
		RobocopyItem ri = new RobocopyItem(0, srcfolder.getRoot().toPath().toAbsolutePath().toString(), "abc");
		rd.setRobocopyItems(Lists.newArrayList(ri));
		return rd;
	}
	
	@Test
	public void tFullBackup() throws JSchException, IOException, SchedulerException, CommandNotFoundException {
		creates();
		createDemoSrc();
		RobocopyDescription rd = grpd();
		
		
		FacadeResult<List<SSHPowershellInvokeResult>> frr = robocopyService.fullBackup(session, server, rd, rd.getRobocopyItems());
		assertThat(frr.getResult().size(), equalTo(1));
		assertThat(frr.getResult().iterator().next().exitCode(), equalTo(1));
		
		Path dstPath = Paths.get(rd.getRobocopyDst("abc"));
		Path afile = dstPath.resolve("a/afile.txt");
		assertTrue(Files.exists(dstPath));
		assertTrue(Files.exists(afile));

		String s= new String(Files.readAllBytes(afile));
		assertThat(s, equalTo("abc"));
		
		/**
		 * compress the robocopydst folder in repofolder to the compressed folder in repofolder.
		 */
		SSHPowershellInvokeResult sshpir = robocopyService.compressArchive(session, server, rd);
		assertThat(sshpir.exitCode(), equalTo(9)); // when compress dst folder does'nt exists.
		
		Path workingCompressed = Paths.get(rd.getWorkingSpaceCompressed());
		Files.createDirectories(workingCompressed);

		sshpir = robocopyService.compressArchive(session, server, rd);
		assertThat(sshpir.exitCode(), equalTo(0));
		
		Path zipPath = Paths.get(rd.getWorkingSpaceCompressed("robocopydst.rar")); 
		assertTrue(Files.exists(zipPath));
		
		Path extractPath = Paths.get(rd.getWorkingSpaceExpanded());
		
		sshpir = robocopyService.expandArchive(session, server, rd, zipPath.toAbsolutePath().toString(), extractPath.toAbsolutePath().toString());
		assertThat(sshpir.exitCode(), equalTo(0));
		
		String rp = rd.getRobocopyDstNoRoot();
		
		// abc folder is one robocopyitem's dst.
		afile = extractPath.resolve(rp).resolve("abc").resolve("a").resolve("afile.txt");
		
		assertTrue(Files.exists(afile));
	}

	
	private void creates() throws JSchException, SchedulerException {
		clearDb();
		createSessionLocalHostWindows();
		deleteAllJobs();
	}

}
