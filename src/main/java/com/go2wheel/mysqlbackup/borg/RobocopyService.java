package com.go2wheel.mysqlbackup.borg;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.go2wheel.mysqlbackup.SettingsInDb;
import com.go2wheel.mysqlbackup.aop.Exclusive;
import com.go2wheel.mysqlbackup.aop.MeasureTimeCost;
import com.go2wheel.mysqlbackup.exception.CommandNotFoundException;
import com.go2wheel.mysqlbackup.exception.ExceptionWrapper;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.exception.ScpException;
import com.go2wheel.mysqlbackup.model.BorgDescription;
import com.go2wheel.mysqlbackup.model.BorgDownload;
import com.go2wheel.mysqlbackup.model.PlayBack;
import com.go2wheel.mysqlbackup.model.PlayBackResult;
import com.go2wheel.mysqlbackup.model.RobocopyDescription;
import com.go2wheel.mysqlbackup.model.RobocopyItem;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.BorgDownloadDbService;
import com.go2wheel.mysqlbackup.service.PlayBackResultDbService;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.util.Md5Checksum;
import com.go2wheel.mysqlbackup.util.RemotePathUtil;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.util.TaskLocks;
import com.go2wheel.mysqlbackup.value.AsyncTaskValue;
import com.go2wheel.mysqlbackup.value.BorgListResult;
import com.go2wheel.mysqlbackup.value.BorgPruneResult;
import com.go2wheel.mysqlbackup.value.CommonMessageKeys;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.value.FacadeResult.CommonActionResult;
import com.go2wheel.mysqlbackup.value.FileInAdirectory;
import com.go2wheel.mysqlbackup.value.FileToCopyInfo;
import com.go2wheel.mysqlbackup.value.LinuxLsl;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.google.common.collect.Lists;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@Service
public class RobocopyService {

	private Logger logger = LoggerFactory.getLogger(getClass());

	public static final String NO_INCLUDES = "borg.archive.noincludes";

	public static final String UNKNOWN = "borg.archive.unknown";

	public static final String REPO_NON_INIT = "borg.repo.noinit";
	public static final String BORG_DOWNLOAD_TASK_KEY = "taskkey.borg.download";
	public static final String BORG_RESTORE_TASK_KEY = "taskkey.borg.restore";

	@Autowired
	private SshSessionFactory sshSessionFactory;

	@Autowired
	private ServerDbService serverDbService;

	@Autowired
	private BorgDownloadDbService borgDownloadDbService;

	@Autowired
	private PlayBackResultDbService playBackResultDbService;

	@Autowired
	private SettingsInDb settingsInDb;

	/**
	 * list all saved versions, choose one to play back.
	 * 
	 * @param sourceServer
	 * @return
	 * @throws IOException
	 */
	public List<BorgRepoWrapper> listLocalRepos(Server sourceServer) throws IOException {
		Path lrp = settingsInDb.getBorgRepoDir(sourceServer);
		List<Path> pathes = Lists.newArrayList();
		pathes = Files.list(lrp.getParent()).collect(toList());
		Collections.sort(pathes, (o1, o2) -> {
			try {
				BasicFileAttributes attr1 = Files.readAttributes(o1, BasicFileAttributes.class);
				BasicFileAttributes attr2 = Files.readAttributes(o2, BasicFileAttributes.class);
				return attr1.lastAccessTime().toInstant().compareTo(attr2.lastAccessTime().toInstant());
			} catch (IOException e) {
				return 0;
			}
		});
		return pathes.stream().map(BorgRepoWrapper::new).collect(toList());
	}

	public static class BorgRepoWrapper {

		private final Path repo;

		public BorgRepoWrapper(Path repo) {
			Assert.notNull(repo, "should not be null.");
			this.repo = repo;
		}

		public String getRepoFolderName() {
			if (repo != null && repo.getFileName() != null) {
				return repo.getFileName().toString();
			} else {
				return "";
			}
		}

		public Date getCreateTime() throws IOException {
			BasicFileAttributes bfa = Files.readAttributes(repo, BasicFileAttributes.class);
			return new Date(bfa.lastAccessTime().toMillis());
		}

		public long getFileCount() throws IOException {
			return Files.walk(repo).count();
		}

		public String getSize() throws IOException {
			long size = Files.walk(repo).filter(Files::isRegularFile).mapToLong(value -> {
				try {
					return Files.size(value);
				} catch (IOException e) {
					return 0L;
				}
			}).sum();
			return StringUtil.formatSize(size, 2);
		}
	}

	public FacadeResult<RemoteCommandResult> initRepo(Session session, String repoPath)
			throws CommandNotFoundException, JSchException, IOException {
		try {
			if (!StringUtil.hasAnyNonBlankWord(repoPath)) {
				return FacadeResult.showMessageUnExpected(CommonMessageKeys.MALFORMED_VALUE, repoPath);
			}
			String command = String.format("borg init --encryption=none %s", repoPath);
			RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, command);
			rcr.isCommandNotFound();
			if (rcr.getExitValue() != 0) {
				boolean isFileNotFound = rcr.getAllTrimedNotEmptyLines().stream()
						.anyMatch(line -> line.contains("FileNotFoundError"));
				if (isFileNotFound) {
					String parentPath = RemotePathUtil.getParentWithEndingSlash(repoPath);
					rcr = SSHcommonUtil.runRemoteCommand(session, String.format("mkdir -p %s", parentPath));
					rcr = SSHcommonUtil.runRemoteCommand(session,
							String.format("borg init --encryption=none %s", repoPath));
				}

				boolean iserrorPath = rcr.getAllTrimedNotEmptyLines().stream()
						.anyMatch(line -> line.contains("argument REPOSITORY: Invalid location format:"));
				if (iserrorPath) {
					return FacadeResult.showMessageUnExpected(CommonMessageKeys.MALFORMED_VALUE, repoPath);
				}

				// Repository /abc already exists.
				boolean alreadyExists = rcr.getAllTrimedNotEmptyLines().stream()
						.anyMatch(line -> line.trim().matches("Repository .* already exists\\.")
								|| line.contains("A repository already exists at"));

				if (alreadyExists) {
					return FacadeResult.showMessageUnExpected(CommonMessageKeys.OBJECT_ALREADY_EXISTS, repoPath);
				}

				// Cannot open self

				// Repository /abc already exists.
				boolean cannotOpenSelf = rcr.getAllTrimedNotEmptyLines().stream()
						.anyMatch(line -> line.contains("Cannot open self"));

				if (cannotOpenSelf) {
					return FacadeResult.showMessageUnExpected(CommonMessageKeys.EXECUTABLE_DAMAGED, repoPath);
				}

				logger.error(command);
				logger.error(String.join("\n", rcr.getAllTrimedNotEmptyLines()));
				return FacadeResult.showMessageUnExpected("ssh.command.failed", command);
			}
			return FacadeResult.doneExpectedResult(rcr, CommonActionResult.DONE);
		} catch (RunRemoteCommandException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}
	}

	public boolean isBorgNotReady(Server server) {
		return server == null || server.getBorgDescription() == null
				|| server.getBorgDescription().getIncludes() == null
				|| server.getBorgDescription().getIncludes().isEmpty();
	}

	//@formatter:off
	public FacadeResult<BorgPruneResult> pruneRepo(Session session, Server server) throws JSchException, IOException {
		try {
			String cmd = String.format("borg prune --list --verbose --prefix %s --show-rc --keep-daily %s --keep-weekly %s --keep-monthly %s %s",
					server.getBorgDescription().getArchiveNamePrefix(), 7, 4, 6,
					server.getBorgDescription().getRepo());
			return FacadeResult.doneExpectedResult(new BorgPruneResult(SSHcommonUtil.runRemoteCommand(session, cmd)), CommonActionResult.DONE);
		} catch (RunRemoteCommandException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}
	}
	
	public CompletableFuture<AsyncTaskValue> downloadRepoAsync(Server server, String taskDescription, Long id) {
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
				FacadeResult<BorgDownload> fr = downloadRepo(session, server);
				BorgDownload bd = fr.getResult();
				bd.setTimeCost(fr.getEndTime() - fr.getStartTime());
				bd.setServerId(server.getId());
				bd = borgDownloadDbService.save(bd);
				fr.setResult(bd);
				return new AsyncTaskValue(id, fr);
			} catch (JSchException | NoSuchAlgorithmException e1) {
				throw new ExceptionWrapper(e1);
			} finally {
				if (session != null && session.isConnected()) {
					session.disconnect();
				}
			}
		}).exceptionally(e -> {
			return new AsyncTaskValue(id, FacadeResult.unexpectedResult(((ExceptionWrapper)e).getException())).withDescription(taskDescription);
		});

	}
	
	//@formatter:off
	@MeasureTimeCost
	public FacadeResult<BorgDownload> downloadRepo(Session session, Server server) throws JSchException, NoSuchAlgorithmException {
		try {
			BorgDescription bd = server.getBorgDescription();
			String findCmd = String.format("find %s -type f -printf '%%s->%%p\n'", bd.getRepo());
			RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, findCmd);
			FileInAdirectory fid = new FileInAdirectory(rcr);

			String md5Cmd = String.format("md5sum %s", fid.getFileNamesSeparatedBySpace());

			rcr = SSHcommonUtil.runRemoteCommand(session, md5Cmd);
			List<String> md5s = rcr.getAllTrimedNotEmptyLines().stream().map(line -> line.split("\\s+", 2))
					.filter(ss -> ss.length == 2).map(ss -> ss[0]).collect(toList());
			fid.setMd5s(md5s);

			final Path rRepo = Paths.get(bd.getRepo());
			final Path localRepo = settingsInDb.getBorgRepoDir(server);

			List<FileToCopyInfo> fis = fid.getFiles().stream().map(fi -> {
				fi.setLfileAbs(localRepo.resolve(rRepo.relativize(Paths.get(fi.getRfileAbs()))));
				return fi;
			}).collect(toList());
			
			BorgDownload bdrs = new BorgDownload();
			bdrs.setTotalFiles(fis.size());
			long totalBytes = 0;
			long downloadBytes = 0;
			int downloadFiles = 0;
			for (FileToCopyInfo fi : fis) {
				if (Files.exists(fi.getLfileAbs())) {
					String m = Md5Checksum.getMD5Checksum(fi.getLfileAbs());
					if (m.equalsIgnoreCase(fi.getMd5())) {
						fi.setDone(true);
						totalBytes += Files.size(fi.getLfileAbs());
						continue;
					}
				}
				SSHcommonUtil.downloadWithTmpDownloadingFile(session, fi.getRfileAbs(), fi.getLfileAbs());
				fi.setDone(true);
				totalBytes += Files.size(fi.getLfileAbs());
				downloadBytes += Files.size(fi.getLfileAbs());
				downloadFiles += 1;
				
			}
			
			bdrs.setDownloadBytes(downloadBytes);
			bdrs.setDownloadFiles(downloadFiles);
			bdrs.setTotalBytes(totalBytes);

			// delete the files already deleted from remote repo.
			List<Path> pathes = Files.walk(localRepo)
					.filter(p -> Files.isRegularFile(p))
					.filter(p -> !fid.fileExists(p))
					.collect(toList());
			
			for(Path p : pathes) {
				Files.delete(p);
			}

			return FacadeResult.doneExpectedResult(bdrs, CommonActionResult.DONE);
		} catch (RunRemoteCommandException | IOException | ScpException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}

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
		SSHcommonUtil.mkdirsp(sessiontarget, serverRepo);
		Path local = settingsInDb.getBorgRepoDir(serverSource).getParent().resolve(localRepo);
		SSHcommonUtil.copyFolder(sessiontarget, local, serverRepo);
		return null;
	}
	
	public RobocopyResult invokeRoboCopyCommand(Session session, String cmd) throws JSchException, IOException {
		String command = cmd + ";$LASTEXITCODE";
		return new RobocopyResult(SSHcommonUtil.runRemoteCommand(session, "GBK", "GBK", command));
	}

	@Exclusive(TaskLocks.TASK_FILEBACKUP)
	public FacadeResult<List<RobocopyResult>> fullBackup(Session session, Server server, RobocopyDescription robocopyDescription, List<RobocopyItem> items) throws CommandNotFoundException, JSchException, IOException {
		
			items.stream().forEach(it -> {
				it.setDstCalced(robocopyDescription.appendToRepo(it.getDstRelative()));
			});
			
			List<RobocopyResult> results = Lists.newArrayList();
			
			for(RobocopyItem item : items) {
				StringBuffer sb = new StringBuffer("Robocopy.exe");
				sb.append(' ').append(item.getSource())
				.append(' ').append(item.getDstCalced())
				.append(' ').append(item.getFileParametersNullSafe());
				
				if (item.getExcludeFilesNullSafe().size() > 0) {
					sb.append(' ').append("/xf ").append(item.getExcludeFilesNullSafe().stream().collect(joining(" ")));					
				}
				
				if (item.getExcludeDirectoriesNullSafe().size() > 0) {
					sb.append(' ').append("/xd ").append(item.getExcludeDirectoriesNullSafe().stream().collect(joining(" ")));							
				}
				
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
				
				sb.append(' ').append("|ForEach-Object {$_.trim()} |Where-Object {$_ -notmatch '.*\\\\$'} | Where-Object {($_ -split  '\\s+').length -gt 2}\", src.toAbsolutePath().toString(), dst.toAbsolutePath().toString())");
				RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, sb.toString());
				results.add(new RobocopyResult(rcr));
			}
			return FacadeResult.doneExpectedResultDone(results);
	}

	public FacadeResult<BorgListResult> listArchives(Session session, Server server) throws CommandNotFoundException, JSchException, IOException {
		return listArchives(session, server.getBorgDescription().getRepo());
	}
	
	
	public FacadeResult<BorgListResult> listArchives(Session session, String repo) throws CommandNotFoundException, JSchException, IOException {
		try {
			RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, String.format("borg list %s", repo));
			rcr.isCommandNotFound();
			return FacadeResult.doneExpectedResult(new BorgListResult(rcr), CommonActionResult.DONE);
		} catch (RunRemoteCommandException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}
	}
	
//	borg info /path/to/repo::2017-06-29T11:00-srv
	public FacadeResult<List<String>> archiveInfo(Session session, Server server, String archive) throws JSchException, IOException {
		try {
			BorgDescription bd = server.getBorgDescription();
			RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, String.format("borg info %s::%s", bd.getRepo(), archive));
			return FacadeResult.doneExpectedResult(rcr.getAllTrimedNotEmptyLines(), CommonActionResult.DONE);
		} catch (RunRemoteCommandException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}
	}

	public FacadeResult<RemoteCommandResult> listRepoFiles(Session session, Server server) throws JSchException, IOException {
		try {
			BorgDescription bd = server.getBorgDescription();
			RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, String.format("ls -lR %s", bd.getRepo()));
			return FacadeResult.doneExpectedResult(rcr, CommonActionResult.DONE);
		} catch (RunRemoteCommandException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}
	}
	
	public static class RobocopyResult {
		
		private final RemoteCommandResult rcr;
		private final List<String> outLines;
		
		private final String lastLine;
		public static RobocopyResult of(RemoteCommandResult rcr) {
			return new RobocopyResult(rcr);
		}
		
		private RobocopyResult(RemoteCommandResult rcr) {
			this.rcr = rcr;
			int sz = rcr.getAllTrimedNotEmptyLines().size();
			outLines = rcr.getAllTrimedNotEmptyLines().stream().limit(sz - 1).collect(toList());
			lastLine = rcr.getAllTrimedNotEmptyLines().get(sz - 1);
		}
		
		/**
		 * different from ssh command exitvalue. 
		 * @return
		 */
		public int exitCode() {
			return Integer.valueOf(lastLine);
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
	
//	//@formatter:on
	//
	// /**
	// *
	// borg产生的卷是有时间顺序的，每个卷都有一个名称，�??以可以恢复到各个时间点�?�prune之后，比如剩�??7个天备份�??4个月备份，那么恢复的粒度就不是每天了�??
	// *
	// * @param session
	// * @param server
	// * @return
	// * @throws CommandNotFoundException
	// * @throws IOException
	// * @throws JSchException
	// */
	// public FacadeResult<?> archive(Session session, Server server) throws
	// CommandNotFoundException, JSchException, IOException {
	// BorgDescription bd = server.getBorgDescription();
	// return archive(session, server, bd.getArchiveNamePrefix(), false);
	// }
	//
	// public FacadeResult<?> backupLocalRepos(Server server) throws IOException {
	// final Path localRepo = settingsInDb.getBorgRepoDir(server);
	// FileUtil.backup(localRepo, 6, settingsInDb.getInteger("borg.repo.backups",
	// 999999), true);
	// return FacadeResult.doneExpectedResult();
	// }
	//
	// public FacadeResult<RemoteCommandResult> archive(Session session, Server
	// server, boolean solveProblems) throws CommandNotFoundException,
	// JSchException, IOException {
	// BorgDescription bd = server.getBorgDescription();
	// return archive(session, server, bd.getArchiveNamePrefix(), solveProblems);
	// }

	public FacadeResult<List<LinuxLsl>> extract(Session sessionTarget, Server serverSource, Server serverTarget,
			/* String sourceRepo, */ String archive, String extractFolder)
			throws RunRemoteCommandException, JSchException, IOException {
		String sourceRepo = serverSource.getBorgDescription().getRepo();
		if (!sourceRepo.endsWith("/")) {
			sourceRepo = sourceRepo + "/";
		}
		if (!archive.startsWith("::")) {
			archive = "::" + archive;
		}
		SSHcommonUtil.deleteRemoteFolder(sessionTarget, extractFolder);
		SSHcommonUtil.mkdirsp(sessionTarget, extractFolder);
		String cmd = String.format("cd %s;borg extract %s%s", extractFolder, sourceRepo, archive);
		RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(sessionTarget, cmd);
		List<String> includes = serverSource.getBorgDescription().getIncludes();
		List<LinuxLsl> extracted = SSHcommonUtil.listRemoteFiles(sessionTarget, extractFolder);
		return FacadeResult.doneExpectedResult(extracted, CommonActionResult.DONE);
	}
}
