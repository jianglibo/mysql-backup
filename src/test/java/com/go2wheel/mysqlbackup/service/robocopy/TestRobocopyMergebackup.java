package com.go2wheel.mysqlbackup.service.robocopy;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.service.RobocopyService;

public class TestRobocopyMergebackup extends RobocopyBaseT {
	
    @Rule
    public TemporaryFolder repofolder= new TemporaryFolder();
    
    @Rule
    public TemporaryFolder srcfolder= new TemporaryFolder();
    
    @Autowired
    private RobocopyService robocopyService;
    
	

	
//	zipPath ： C:\Users\ADMINI~1\AppData\Local\Temp\junit9190450242082458614\workingspace\hello.rar
//	expanded： C:\Users\ADMINI~1\AppData\Local\Temp\junit9190450242082458614\workingspace\expanded
//	noroot： Users/ADMINI~1/AppData/Local/Temp/junit9190450242082458614/robocopydst
	
	/**
	 * it means the expanded folder start with "Users/ADMINI~1/AppData/Local/Temp/junit9190450242082458614/robocopydst", we must enter this folder, then we can find "abc" folder which maps to some other defined folder 'c:\akk',
	 * we create c:\akk then copy all files in 'abc' into it.
	 * 
	 * @throws JSchException
	 * @throws IOException
	 * @throws SchedulerException
	 * @throws CommandNotFoundException
	 * @throws NoSuchAlgorithmException
	 * @throws UnExpectedOutputException
	 * @throws RunRemoteCommandException
	 * @throws ScpException
	 * @throws UnExpectedInputException 
	 */
	
//	@Test
//	public void tFullBackupStep() throws JSchException, IOException, SchedulerException, CommandNotFoundException, NoSuchAlgorithmException, UnExpectedOutputException, RunRemoteCommandException, ScpException, UnExpectedInputException {
//		createSessionLocalHostWindowsAfterClear();
//		
//		// create demo file.
//		Path rt = srcfolder.getRoot().toPath();
//		createALocalFile(rt.resolve("a/afile.txt"), "abc");
//		
//		// the source is rt, but has two destinations 'abc' and 'abc1';   
//		RobocopyDescription robocopyDescription = grpd(repofolder, srcfolder);
//		
//		// delete existing local repo.
//		Files.list(settingsIndb.getCurrentRepoDir(server)).forEach(f -> {
//			try {
//				Files.delete(f);
//			} catch (IOException e) {
//			}
//		});
//		
//		Path r = robocopyService.fullBackup(session, server, robocopyDescription, robocopyDescription.getRobocopyItems());
//		
//		Path lrepo = settingsIndb.getCurrentRepoDir(server);
//		List<Path> files = Files.list(lrepo).collect(Collectors.toList());
//		// only has a fullbakcup.rar file.
//		assertThat(files.size(), equalTo(1));
//		
//		// then add a new file.
//		createALocalFile(rt.resolve("a/afile1.txt"), "abc");
//		robocopyService.incrementalBackupAndDownload(session, server, robocopyDescription, robocopyDescription.getRobocopyItems());
//		files = Files.list(lrepo).collect(Collectors.toList());
//		assertThat("should has 2 files, one fullbackup and an incremental.", files.size(), equalTo(2));
//		
//		// then change a file of content.
//		Files.write(rt.resolve("a/afile.txt"), "kkk".getBytes());
//		robocopyService.incrementalBackupAndDownload(session, server, robocopyDescription, robocopyDescription.getRobocopyItems());
//		files = Files.list(lrepo).collect(Collectors.toList());
//		assertThat("should has 3 files, one fullbackup and two incremental.", files.size(), equalTo(3));
//		
//		robocopyService.expandLocalArchive(server, robocopyDescription, settingsIndb.getCurrentRepoDir(server));
//		
//		FileUtil.deleteFolder(settingsIndb.getRepoTmp(server), true);
//	
//	}
}
