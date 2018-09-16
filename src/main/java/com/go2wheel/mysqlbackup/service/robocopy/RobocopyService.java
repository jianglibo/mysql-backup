package com.go2wheel.mysqlbackup.service.robocopy;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.go2wheel.mysqlbackup.SettingsInDb;
import com.go2wheel.mysqlbackup.aop.Exclusive;
import com.go2wheel.mysqlbackup.event.ModelDeletedEvent;
import com.go2wheel.mysqlbackup.exception.CommandNotFoundException;
import com.go2wheel.mysqlbackup.exception.ExceptionWrapper;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.exception.ScpException;
import com.go2wheel.mysqlbackup.exception.UnExpectedOutputException;
import com.go2wheel.mysqlbackup.exception.UnExpectedInputException;
import com.go2wheel.mysqlbackup.model.BorgDescription;
import com.go2wheel.mysqlbackup.model.PlayBack;
import com.go2wheel.mysqlbackup.model.PlayBackResult;
import com.go2wheel.mysqlbackup.model.RobocopyDescription;
import com.go2wheel.mysqlbackup.model.RobocopyItem;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.PlayBackResultDbService;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.util.PSUtil;
import com.go2wheel.mysqlbackup.util.PathUtil;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.util.TaskLocks;
import com.go2wheel.mysqlbackup.value.AsyncTaskValue;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.value.FacadeResult.CommonActionResult;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.google.common.collect.Lists;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@Service
public class RobocopyService {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private SshSessionFactory sshSessionFactory;

	@Autowired
	private ServerDbService serverDbService;

	@Autowired
	private PlayBackResultDbService playBackResultDbService;

	@Autowired
	private SettingsInDb settingsInDb;
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private ObjectMapper objectMapper;


//	public FacadeResult<?> backupLocalRepos(Server server) throws IOException {
//		final Path localRepo = settingsInDb.getCurrentRepoDir(server);
//		FileUtil.backup(localRepo, 6, settingsInDb.getInteger("borg.repo.backups", 999999), false);
//		return FacadeResult.doneExpectedResult();
//	}

	@EventListener
	public void whenRobocopyDescriptionDelete(ModelDeletedEvent<RobocopyDescription> rde) {

	}
	
	/**
	 * increamental archive is a fixed name.
	 * @param session
	 * @param server
	 * @param robocopyDescription
	 * @param items
	 * @return
	 * @throws CommandNotFoundException
	 * @throws JSchException
	 * @throws IOException
	 * @throws UnExpectedOutputException
	 * @throws RunRemoteCommandException
	 * @throws NoSuchAlgorithmException
	 * @throws ScpException
	 * @throws UnExpectedInputException 
	 */
	public FacadeResult<Path> downloadIncreamentalArchive(Session session, Server server, RobocopyDescription robocopyDescription, List<RobocopyItem> items) throws CommandNotFoundException, JSchException, IOException, UnExpectedOutputException, RunRemoteCommandException, NoSuchAlgorithmException, ScpException, UnExpectedInputException {
		robocopyDescription.modifiItems(items);
		
		Path currentRepo = settingsInDb.getCurrentRepoDir(server);
		Path comppressedArchive = currentRepo.resolve(PathUtil.getFileName(robocopyDescription.getWorkingSpaceCompressedArchive()));
		
		if (!Files.exists(comppressedArchive)) {
			fullBackup(session, server, robocopyDescription, items);
			// no need to do this increment backup.
			return FacadeResult.doneExpectedResult(comppressedArchive, CommonActionResult.DONE);
		}
		
		String increamentalArchive = robocopyDescription.getWorkingSpaceIncreamentalArchive();
		String cmd = String.format("Get-Item -Path %s | Get-FileHash -Algorithm MD5 | Format-List", increamentalArchive);
		
		RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, cmd);
		
		if(rcr.getExitValue() != 0) {
			throw new UnExpectedOutputException("1000", "powershell.getitem", rcr.getAllTrimedNotEmptyLines().stream().collect(joining("\n")));
		}
		
		Map<String, String> md5Item = PSUtil.parseFormatList(rcr.getAllTrimedNotEmptyLines()).get(0);

		final String rFile = md5Item.get("Path");
		
		Path localPaht = currentRepo.resolve(PathUtil.getFileName(rFile));
		
		localPaht = PathUtil.getNextAvailable(localPaht, 7);
		
		Path dd = SSHcommonUtil.downloadWithTmpDownloadingFile(server.getOs(), session, rFile, md5Item.get("Hash"), localPaht);
		return FacadeResult.doneExpectedResult(dd, CommonActionResult.DONE);
	}

	
	public FacadeResult<Path> downloadCompressed(Session session, Server server, RobocopyDescription robocopyDescription) throws JSchException, NoSuchAlgorithmException, UnExpectedOutputException, RunRemoteCommandException, IOException, ScpException {
		String compressedArchive = robocopyDescription.getWorkingSpaceCompressedArchive();
		String cmd = String.format("Get-Item -Path %s | Get-FileHash -Algorithm MD5 | Format-List", compressedArchive);
		
		RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, cmd);
		
		if(rcr.getExitValue() != 0) {
			throw new UnExpectedOutputException("1000", "powershell.getitem", rcr.getAllTrimedNotEmptyLines().stream().collect(joining("\n")));
		}
		
		Map<String, String> md5Item = PSUtil.parseFormatList(rcr.getAllTrimedNotEmptyLines()).get(0);
		

		final String rRepo = md5Item.get("Path");
		final Path localRepo = settingsInDb.getCurrentRepoDir(server).resolve(PathUtil.getFileName(rRepo));
		Path dd = SSHcommonUtil.downloadWithTmpDownloadingFile(server.getOs(), session, rRepo, md5Item.get("Hash"), localRepo);
		return FacadeResult.doneExpectedResult(dd, CommonActionResult.DONE);
	}
	
	
	public CompletableFuture<AsyncTaskValue> playbackAsync(PlayBack playback, String localRepo, Long id) {
		Server source = serverDbService.findById(playback.getSourceServerId());
		Server target = serverDbService.findById(playback.getTargetServerId());
		return CompletableFuture.supplyAsync(() -> {
			Session[] sessions = new Session[2];
			try {
				sessions[0] = sshSessionFactory.getConnectedSession(source).getResult();
				sessions[1] = sshSessionFactory.getConnectedSession(target).getResult();
			} catch (JSchException e) {
				SshSessionFactory.closeSession(sessions[0]);
				SshSessionFactory.closeSession(sessions[1]);
				throw new ExceptionWrapper(e);
			}
			return sessions;
		}).thenApplyAsync(sessions -> {
			try {
				FacadeResult<PlayBackResult> fr = playback(sessions[0], sessions[1], source, target, playback, localRepo);
				PlayBackResult bd = fr.getResult();
				bd = playBackResultDbService.save(bd);
				fr.setResult(bd);
				return new AsyncTaskValue(id, fr);
			} catch (IOException | RunRemoteCommandException | JSchException e) {
				throw new ExceptionWrapper(e);
			} finally {
				SshSessionFactory.closeSession(sessions[0]);
				SshSessionFactory.closeSession(sessions[1]);
			}
		}).exceptionally(t -> {
			Throwable tt = t.getCause();
			Throwable ttt = tt;
			if (tt instanceof ExceptionWrapper) {
				ttt = ((ExceptionWrapper) tt).getException();
			}
			ExceptionUtil.logErrorException(logger, ttt);
			
			return new AsyncTaskValue(id, FacadeResult.unexpectedResult(ttt));
		});
	}
	
	public FacadeResult<PlayBackResult> playbackSync(PlayBack playback, String localRepo) throws IOException, RunRemoteCommandException, JSchException {
		Server serverSource = serverDbService.findById(playback.getSourceServerId());
		Server serverTarget = serverDbService.findById(playback.getTargetServerId());
		Session[] sessions = new Session[2];
		try {
			sessions[0] = sshSessionFactory.getConnectedSession(serverSource).getResult();
			sessions[1] = sshSessionFactory.getConnectedSession(serverTarget).getResult();
		} catch (JSchException e) {
			SshSessionFactory.closeSession(sessions[0]);
			SshSessionFactory.closeSession(sessions[1]);
			throw new ExceptionWrapper(e);
		}
		return playback(sessions[0], sessions[1], serverSource, serverTarget, playback, localRepo);
	}
	
	public FacadeResult<PlayBackResult> playback(Session sessionSource, Session sessiontarget, Server serverSource, Server serverTarget, PlayBack playback, String localRepo) throws IOException, RunRemoteCommandException, JSchException {
		// from server get borgdescription, get repo properties. Create a directory on playback server. and upload local repo. Then invoke borg command on that server, listing extracting.
		if (serverSource.getBorgDescription() == null) {
			serverSource = serverDbService.loadFull(serverSource);
		}
		BorgDescription bd = serverSource.getBorgDescription();
		String serverRepo = bd.getRepo();
		SSHcommonUtil.mkdirsp(serverTarget.getOs(), sessiontarget, serverRepo);
		Path local = settingsInDb.getCurrentRepoDir(serverSource).getParent().resolve(localRepo);
		SSHcommonUtil.copyFolder(serverTarget.getOs(), sessiontarget, local, serverRepo);
		return null;
	}
	
	public SSHPowershellInvokeResult invokeRoboCopyCommand(Session session, String cmd) throws JSchException, IOException {
		String command = cmd + ";$LASTEXITCODE";
		return new SSHPowershellInvokeResult(SSHcommonUtil.runRemoteCommand(session, "GBK", "GBK", command));
	}
	
	/**
	 * The source is getRobocopyDst(), The dst is the name of the source.
	 * But .rar or .zip?
	 * @param session
	 * @param server
	 * @param robocopyDescription
	 * @return
	 * @throws JSchException
	 * @throws IOException
	 */
	public SSHPowershellInvokeResult compressArchive(Session session, Server server, RobocopyDescription robocopyDescription) throws JSchException, IOException {
//		& 'C:/Program Files/WinRAR/Rar.exe' a C:/Users/ADMINI~1/AppData/Local/Temp/junit5949670059685808788/workingspace/compressed C:/Users/ADMINI~1/AppData/Local/Temp/junit5949670059685808788/robocopydst;$LASTEXITCODE
		String cmd = robocopyDescription.getCompressCommandInstance();
		return SSHPowershellInvokeResult.of(SSHcommonUtil.runRemoteCommand(session, "GBK", null, cmd));
	}
	
	public SSHPowershellInvokeResult expandArchive(Session session, Server server, RobocopyDescription robocopyDescription, String archive, String expandDst) throws JSchException, IOException {
		archive = archive.replace('\\', '/');
		expandDst = expandDst.replace('\\', '/');
		if (!expandDst.endsWith("/")) {
			expandDst += "/";
		}
		// expandDst must have directory trail. / or \.
		String cmd = String.format(robocopyDescription.getExpandCommand(), archive, expandDst);
		if (!cmd.startsWith("&")) {
			cmd = "& " + cmd;
		}
		cmd = cmd + ";$LASTEXITCODE";
		return SSHPowershellInvokeResult.of(SSHcommonUtil.runRemoteCommand(session, "GBK", null, cmd));
		
//		& 'C:/Program Files/WinRAR/Rar.exe' x -o+ C:\Users\ADMINI~1\AppData\Local\Temp\junit6021286854869517036\workingspace\compressed\robocopydst.rar C:\Users\ADMINI~1\AppData\Local\Temp\junit6021286854869517036\workingspace\expanded
	}
	
	public List<String> getBackupScripts(Session session, Server server, RobocopyDescription robocopyDescription, List<RobocopyItem> items) {
		robocopyDescription.modifiItems(items);
		List<String> results = Lists.newArrayList();
		
		for(RobocopyItem item : items) {
			StringBuffer sb = new StringBuffer("Robocopy.exe");
			sb.append(' ').append(item.getSourceSlash())
			.append(' ').append(item.getDstCalced())
			.append(' ').append(item.getFileParametersNullSafe());
			
			if (item.getExcludeFilesNullSafe().size() > 0) {
				sb.append(' ').append("/xf ").append(item.getExcludeFilesNullSafe().stream().collect(joining(" ")));					
			}
			
			if (item.getExcludeDirectoriesNullSafe().size() > 0) {
				sb.append(' ').append("/xd ").append(item.getExcludeDirectoriesNullSafe().stream().collect(joining(" ")));							
			}
			
			sb.append(" /log+:").append(robocopyDescription.getWorkingSpaceRoboLog());
			
			/**
			 * /xf and /xd are special and excluded.
			 */
			if (item.getFileSelectionOptionsNullSafe().size() > 0) {
				sb.append(' ').append(item.getFileSelectionOptionsNullSafe().stream().collect(joining(" ")));							
			}
			
			if (item.getCopyOptionsNullSafe().size() > 0) {
				sb.append(' ').append(item.getCopyOptionsNullSafe().stream().collect(joining(" ")));							
			}
			
			if (item.getLoggingOptionsNullSafe().size() > 0) {
				sb.append(' ').append(item.getLoggingOptionsNullSafe().stream().collect(joining(" ")));							
			}
//			sb.append(' ').append("|ForEach-Object {$_.trim()} |Where-Object {$_ -notmatch '.*\\\\$'} | Where-Object {($_ -split  '\\s+').length -gt 2}\", src.toAbsolutePath().toString(), dst.toAbsolutePath().toString())");
//			RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, sb.toString());
			results.add(sb.toString());
		}
		return results;
	}

	

	/**
	 * repo stores robocopied folders. workingspace has two subdirectories, compressed for ziped file, expanded for extracted files.
	 * 
	 * @param session
	 * @param server
	 * @param robocopyDescription
	 * @param items
	 * @return
	 * @throws CommandNotFoundException
	 * @throws JSchException
	 * @throws IOException
	 */
	@Exclusive(TaskLocks.TASK_FILEBACKUP)
	public FacadeResult<List<SSHPowershellInvokeResult>> executeRobocopies(Session session, Server server, RobocopyDescription robocopyDescription, List<RobocopyItem> items) throws CommandNotFoundException, JSchException, IOException {
		List<String> individualCommands = getBackupScripts(session, server, robocopyDescription, items);
		List<SSHPowershellInvokeResult> results = Lists.newArrayList();
		for(String command: individualCommands) {
			results.add(invokeRoboCopyCommand(session, command));
		}
		return FacadeResult.doneExpectedResultDone(results);
	}
	
	
	public SSHPowershellInvokeResult increamentalBackup(Session session, Server server, RobocopyDescription robocopyDescription, List<RobocopyItem> items) throws CommandNotFoundException, JSchException, IOException, UnExpectedInputException, UnExpectedOutputException {
		if (items == null || items.isEmpty()) {
			throw new UnExpectedInputException("1000", "validator.emptyornull", "No Source directories assigned.");
		}
		robocopyDescription.modifiItems(items);
		String cmd = robocopyDescription.getWorkingSpaceScriptFile() + " -action increamental";
		SSHPowershellInvokeResult sshir =  SSHPowershellInvokeResult.of(SSHcommonUtil.runRemoteCommand(session, "GBK", null, cmd));
		if (sshir.isCommandNotFound()) {
			copyScriptToServer(session, server, robocopyDescription, items);
			sshir =  SSHPowershellInvokeResult.of(SSHcommonUtil.runRemoteCommand(session, "GBK", null, cmd));
		}
		if (sshir.exitCode() > 8) {
			throw new UnExpectedOutputException("1000", "", sshir.getRcr().getAllTrimedNotEmptyLines().stream().collect(joining("\n")), sshir.getRcr().getCommand());
		}
		return sshir;
	}
	
	public Path increamentalBackupAndDownload(Session session, Server server, RobocopyDescription robocopyDescription, List<RobocopyItem> items) throws UnExpectedInputException, CommandNotFoundException, JSchException, IOException, UnExpectedOutputException, RunRemoteCommandException, NoSuchAlgorithmException, ScpException {
		SSHPowershellInvokeResult sshir =  increamentalBackup(session, server, robocopyDescription, items);
		if (sshir.exitCode() != -1) {
			FacadeResult<Path> fp = downloadIncreamentalArchive(session, server, robocopyDescription, items);
			return fp.getResult();
		}
		return null;
	}
	
	private String serializeToJson(RobocopyDescription robocopyDescription) throws JsonProcessingException {
		ObjectNode jn = objectMapper.valueToTree(robocopyDescription);
		String cc = robocopyDescription.getCompressCommand();
		jn.set("compressExe", new TextNode(StringUtil.extractExecutable(cc)));
		String s = objectMapper.writeValueAsString(jn); 
		return s;
	}
	
	public Path fullBackup(Session session, Server server, RobocopyDescription robocopyDescription, List<RobocopyItem> items) throws CommandNotFoundException, JSchException, IOException, NoSuchAlgorithmException, UnExpectedOutputException, RunRemoteCommandException, ScpException, UnExpectedInputException {
		if (items == null || items.isEmpty()) {
			throw new UnExpectedInputException("1000", "validator.emptyornull", "No Source directories assigned.");
		}
		copyScriptToServer(session, server, robocopyDescription, items);
		FacadeResult<List<SSHPowershellInvokeResult>> fr = executeRobocopies(session, server, robocopyDescription, items);
		
		for(SSHPowershellInvokeResult sshir : fr.getResult()) {
			if (sshir.hasError()) {
				throw new UnExpectedOutputException("", "", sshir.getRcr().getAllTrimedNotEmptyLines().stream().collect(joining("\n")));
			}
		}
		SSHPowershellInvokeResult rcr = compressArchive(session, server, robocopyDescription);
		if (rcr.exitCode() != 0) {
			throw new UnExpectedOutputException("1000", "powershell.robocopy.compress", rcr.getRcr().getAllTrimedNotEmptyLines().stream().collect(joining("\n")));
		}
		return downloadCompressed(session, server, robocopyDescription).getResult();
	}
	
	//@formatter:off
	public CompletableFuture<AsyncTaskValue> fullBackupAsync(Server server, RobocopyDescription robocopyDescription, List<RobocopyItem> items, String taskDescription, Long id) {
		return CompletableFuture.supplyAsync(() -> {
			FacadeResult<Session> frSession;
			try {
				frSession = sshSessionFactory.getConnectedSession(server);
			} catch (JSchException e) {
				throw new ExceptionWrapper(e);
			}
			return frSession.getResult();
		}).thenApplyAsync(session -> {
			try {
				Path arPath = fullBackup(session, server, robocopyDescription, items);
				return new AsyncTaskValue(id, arPath);
			} catch (JSchException | NoSuchAlgorithmException | UnExpectedOutputException | RunRemoteCommandException | IOException | ScpException | CommandNotFoundException | UnExpectedInputException e1) {
				throw new ExceptionWrapper(e1);
			} finally {
				if (session != null && session.isConnected()) {
					session.disconnect();
				}
			}
		}).exceptionally(e -> {
			ExceptionWrapper we = (ExceptionWrapper) e.getCause();
			return new AsyncTaskValue(id, FacadeResult.unexpectedResult(we.getException())).withDescription(taskDescription);
		});
	}

	protected void copyScriptToServer(Session session, Server server, RobocopyDescription robocopyDescription,
			List<RobocopyItem> items) throws JsonProcessingException, JSchException, IOException {
		items = robocopyDescription.modifiItems(items);
		Resource r = applicationContext.getResource("classpath:powershell/robocopyjob.ps1");
		try(InputStream is = r.getInputStream()) {
			List<String> scriptLines = StringUtil.splitLines(StringUtil.inputstreamToString(is));
			List<String> afterLines = Lists.newArrayList();
//			# assign_line{RobocopyDescription}
			for(String line: scriptLines) {
				String assignName = StringUtil.parseAssignLine(line);
				if (assignName != null) {
//					String s = String.format("$%s=%s",assignName,StringUtil.espacePowershellString(objectMapper.writeValueAsString(robocopyDescription)));
					//here document.
					if ("robocopyDescription".equals(assignName)) {
						String s = String.format("$%s=@\"", assignName);
						afterLines.add(s);
						afterLines.add(serializeToJson(robocopyDescription));
						afterLines.add("\"@");
					} else if ("robocopies".equals(assignName)) {
						List<String> sl =  getBackupScripts(session, server, robocopyDescription, items);
						String s = String.format("$%s='%s'", assignName, sl.stream().collect(joining(",")));
						afterLines.add(s);
					}
				} else {
					afterLines.add(line);
				}
			}
			byte[] bb = afterLines.stream().collect(joining("\n")).getBytes();
			SSHcommonUtil.copy("win", session, robocopyDescription.getWorkingSpaceScriptFile(), bb);
		}
	}

	
	public static class SSHPowershellInvokeResult {
		
		private final RemoteCommandResult rcr;
		private final List<String> outLines;
		
		private final String lastLine;
		public static SSHPowershellInvokeResult of(RemoteCommandResult rcr) {
			return new SSHPowershellInvokeResult(rcr);
		}
		
		private SSHPowershellInvokeResult(RemoteCommandResult rcr) {
			this.rcr = rcr;
			List<String> all = rcr.getAllTrimedNotEmptyLinesErrorFirst();
			int sz = all.size();
			
			outLines = all.stream().limit(sz - 1).collect(toList());
			lastLine = all.get(sz - 1);
		}
		
		/**
		 * different from ssh command exitvalue. 
		 * @return
		 */
		public int exitCode() {
			return Integer.valueOf(lastLine);
		}
		
		public boolean isCommandNotFound() {
			return rcr.getAllTrimedNotEmptyLines().stream().anyMatch(line -> line.indexOf("CommandNotFoundException") != -1);
		}
		
		public boolean hasError() {
			return exitCode() > 8;
		}

		public RemoteCommandResult getRcr() {
			return rcr;
		}

		public List<String> getOutLines() {
			return outLines;
		}

		public String getLastLine() {
			return lastLine;
		}
	}
}
