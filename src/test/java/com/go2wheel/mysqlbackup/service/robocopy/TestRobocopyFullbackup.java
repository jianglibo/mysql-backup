package com.go2wheel.mysqlbackup.service.robocopy;

public class TestRobocopyFullbackup extends RobocopyBaseT {
	
//    @Rule
//    public TemporaryFolder repofolder= new TemporaryFolder();
//    
//    @Rule
//    public TemporaryFolder srcfolder= new TemporaryFolder();
//    
//    @Autowired
//    private RobocopyService robocopyService;
//    
//	
//	@Test
//	public void tjsonfy() throws  SchedulerException, IOException {
//		createSessionLocalHostWindowsAfterClear();
//		RobocopyDescription robocopyDescription = grpd(repofolder, srcfolder);
//		
//		String json = objectMapper.writeValueAsString(robocopyDescription);
//		json = StringUtil.espacePowershellString(json);
//		
//		String[] names = applicationContext.getBeanNamesForType(ObjectMapper.class);
//		assertThat(names.length, equalTo(1));
//		
//		List<String> lines = StringUtil.splitLines(json);
//		assertThat(lines.size(), equalTo(1));
//	}
//	
//	@Test
//	public void tIncreamental() throws IOException,  SchedulerException, NoSuchAlgorithmException, UnExpectedOutputException, CommandNotFoundException, RunRemoteCommandException, ScpException, UnExpectedInputException {
//		createSessionLocalHostWindowsAfterClear();
//		createDemoSrc(srcfolder);
//		RobocopyDescription robocopyDescription = grpd(repofolder, srcfolder);
//		
//		assertFalse(Files.exists(Paths.get(robocopyDescription.getWorkingSpaceChangeList())));
//		assertFalse(Files.exists(Paths.get(robocopyDescription.getWorkingSpaceIncreamentalArchive())));
//		robocopyService.copyScriptToServer(session, server, robocopyDescription, robocopyDescription.getRobocopyItems());
//		SSHPowershellInvokeResult b = robocopyService.increamentalBackup(session, server, robocopyDescription, robocopyDescription.getRobocopyItems());
//		assertThat(b.exitCode(), equalTo(0));
//		
//		assertTrue(Files.exists(Paths.get(robocopyDescription.getWorkingSpaceChangeList())));
//		assertTrue(Files.exists(Paths.get(robocopyDescription.getWorkingSpaceIncreamentalArchive())));
//		
//		FacadeResult<Path> fp = robocopyService.downloadIncreamentalArchive(session, server, robocopyDescription, robocopyDescription.getRobocopyItems());
//		assertThat(Files.size(fp.getResult()), equalTo(Files.size(Paths.get(robocopyDescription.getWorkingSpaceIncreamentalArchive()))));
//		
//	}
//	@Test
//	public void tFullback() throws IOException,  SchedulerException, NoSuchAlgorithmException, UnExpectedOutputException, CommandNotFoundException, RunRemoteCommandException, ScpException, UnExpectedInputException {
//		createSessionLocalHostWindowsAfterClear();
//		createDemoSrc(srcfolder);
//		RobocopyDescription robocopyDescription = grpd(repofolder, srcfolder);
//		robocopyService.fullBackup(session, server, robocopyDescription, robocopyDescription.getRobocopyItems());
//		assertTrue(Files.exists(settingsIndb.getCurrentRepoDir(server).resolve(PathUtil.getFileName(robocopyDescription.getWorkingSpaceCompressedArchive()))));
//		assertTrue(Files.exists(Paths.get(robocopyDescription.getWorkingSpaceScriptFile())));
//		
//		RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, robocopyDescription.getWorkingSpaceScriptFile() + " -action echo");
//		assertThat(rcr.getExitValue(), equalTo(0));
//		
//		assertThat(rcr.getAllTrimedNotEmptyLines().get(0), equalTo("echo"));
//	}
//
//	
////	zipPath ： C:\Users\ADMINI~1\AppData\Local\Temp\junit9190450242082458614\workingspace\hello.rar
////	expanded： C:\Users\ADMINI~1\AppData\Local\Temp\junit9190450242082458614\workingspace\expanded
////	noroot： Users/ADMINI~1/AppData/Local/Temp/junit9190450242082458614/robocopydst
//	
//	/**
//	 * it means the expanded folder start with "Users/ADMINI~1/AppData/Local/Temp/junit9190450242082458614/robocopydst", we must enter this folder, then we can find "abc" folder which maps to some other defined folder 'c:\akk',
//	 * we create c:\akk then copy all files in 'abc' into it.
//	 * 
//	 * @throws JSchException
//	 * @throws IOException
//	 * @throws SchedulerException
//	 * @throws CommandNotFoundException
//	 * @throws NoSuchAlgorithmException
//	 * @throws UnExpectedOutputException
//	 * @throws RunRemoteCommandException
//	 * @throws ScpException
//	 */
//	
//	@Test
//	public void tFullBackupStep() throws  IOException, SchedulerException, CommandNotFoundException, NoSuchAlgorithmException, UnExpectedOutputException, RunRemoteCommandException, ScpException {
//		createSessionLocalHostWindowsAfterClear();
//		createDemoSrc(srcfolder);
//		RobocopyDescription robocopyDescription = grpd(repofolder, srcfolder);
//		
//		Files.list(settingsIndb.getCurrentRepoDir(server)).forEach(f -> {
//			try {
//				Files.delete(f);
//			} catch (IOException e) {
//			}
//		});
//
//		
//		List<String> scripts = robocopyService.getBackupScripts(session, server, robocopyDescription, robocopyDescription.getRobocopyItems());
//		String s = scripts.stream().collect(Collectors.joining(";"));
//		assertThat(scripts.size(), equalTo(2));
//		
//		
//		FacadeResult<List<SSHPowershellInvokeResult>> frr = robocopyService.executeRobocopies(session, server, robocopyDescription, robocopyDescription.getRobocopyItems());
//		assertThat(frr.getResult().size(), equalTo(2));
//		assertThat(frr.getResult().iterator().next().exitCode(), equalTo(1));
//		
//		Path dstPath = Paths.get(robocopyDescription.getRobocopyItemDst("abc"));
//		Path afile = dstPath.resolve("a/afile.txt");
//		assertTrue(Files.exists(dstPath));
//		assertTrue(Files.exists(afile));
//
//		s= new String(Files.readAllBytes(afile));
//		assertThat(s, equalTo("abc"));
//		
//		/**
//		 * compress the robocopydst folder in repofolder to the compressed folder in repofolder.
//		 */
//		SSHPowershellInvokeResult sshpir = robocopyService.compressArchive(session, server, robocopyDescription);
//		assertThat(sshpir.exitCode(), equalTo(0));
//		
//		Path zipPath = Paths.get(robocopyDescription.getWorkingSpaceCompressedArchive()); 
//		assertTrue(Files.exists(zipPath));
//		
//		Path extractPath = Paths.get(robocopyDescription.getWorkingSpaceExpanded());
//		
//		sshpir = robocopyService.expandArchive(session, server, robocopyDescription, zipPath.toAbsolutePath().toString(), extractPath.toAbsolutePath().toString());
//		assertThat(sshpir.exitCode(), equalTo(0));
//		
//		String rp = robocopyDescription.getRobocopyDstNoRoot();
//		
//		// abc folder is one robocopyitem's dst.
//		afile = extractPath.resolve(rp).resolve("abc").resolve("a").resolve("afile.txt");
//		
//		assertTrue(Files.exists(afile));
//		
//		FacadeResult<Path> p = robocopyService.downloadCompressed(session, server, robocopyDescription);
//		assertTrue(Files.exists(p.getResult()));
//		assertThat(p.getResult().getFileName().toString(), equalTo(robocopyDescription.getArchiveName()));
//		p = robocopyService.downloadCompressed(session, server, robocopyDescription); // download again.
//		assertTrue(Files.exists(p.getResult()));
//		assertThat(Files.list(settingsIndb.getCurrentRepoDir(server)).count(), equalTo(1L));
//	}
}
