package com.go2wheel.mysqlbackup.borg;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.go2wheel.mysqlbackup.MyAppSettings;
import com.go2wheel.mysqlbackup.aop.Exclusive;
import com.go2wheel.mysqlbackup.aop.MeasureTimeCost;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.exception.ScpException;
import com.go2wheel.mysqlbackup.exception.UnExpectedContentException;
import com.go2wheel.mysqlbackup.http.FileDownloader;
import com.go2wheel.mysqlbackup.installer.BorgInstallInfo;
import com.go2wheel.mysqlbackup.installer.BorgInstaller;
import com.go2wheel.mysqlbackup.model.BorgDescription;
import com.go2wheel.mysqlbackup.model.BorgDownload;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.util.Md5Checksum;
import com.go2wheel.mysqlbackup.util.RemotePathUtil;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.util.TaskLocks;
import com.go2wheel.mysqlbackup.value.BorgListResult;
import com.go2wheel.mysqlbackup.value.BorgPruneResult;
import com.go2wheel.mysqlbackup.value.CommonMessageKeys;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.value.FacadeResult.CommonActionResult;
import com.go2wheel.mysqlbackup.value.FileInAdirectory;
import com.go2wheel.mysqlbackup.value.FileInfo;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.jcraft.jsch.Session;

@Service
public class BorgService {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	public static final String NO_INCLUDES = "borg.archive.noincludes";
	
	public static final String UNKNOWN = "borg.archive.unknown";
	
	public static final String REPO_NON_INIT = "borg.repo.noinit";
	
	
	@Autowired
	private BorgInstaller borgInstaller;
	
	

	// https://borgbackup.readthedocs.io/en/stable/quickstart.html
	// sudo cp borg-linux64 /usr/local/bin/borg

	// sudo chown root:root /usr/local/bin/borg
	// sudo chmod 755 /usr/local/bin/borg
	// ln -s /usr/local/bin/borg /usr/local/bin/borgfs

	public static final String BORG_BINARY_URL = "https://github.com/borgbackup/borg/releases/download/1.1.5/borg-linux64";
	public static final String REMOTE_BORG_BINARY = "/usr/local/bin/borg";

	private MyAppSettings appSettings;

	private FileDownloader fileDownloader;

	private BorgInstallInfo getBorgInstallInfo(Session session) throws RunRemoteCommandException {
		BorgInstallInfo ii = new BorgInstallInfo();
		RemoteCommandResult rcr;
		rcr = SSHcommonUtil.runRemoteCommand(session, "which borg;borg -V");
		if (rcr.getExitValue() == 0) {
			List<String> lines = rcr.getAllTrimedNotEmptyLines();
			ii.setExecutable(lines.get(0));
			ii.setVersion(lines.get(1));
			ii.setInstalled(true);
		}
		return ii;
	}

//	public FacadeResult<BorgInstallInfo> unInstall(Session session) {
//		try {
//			BorgInstallInfo ii = getBorgInstallInfo(session);
//			if (ii.isInstalled()) {
//				SSHcommonUtil.deleteRemoteFile(session, REMOTE_BORG_BINARY);
//				return FacadeResult.doneExpectedResult(getBorgInstallInfo(session), CommonActionResult.DONE);
//			} else {
//				return FacadeResult.doneExpectedResult(ii, CommonActionResult.PREVIOUSLY_DONE);
//			}
//		} catch (RunRemoteCommandException e) {
//			ExceptionUtil.logErrorException(logger, e);
//			return FacadeResult.unexpectedResult(e);
//		}
//	}

//	public FacadeResult<BorgInstallInfo> install(Session session) {
//		BorgInstallInfo ii;
//		try {
//			ii = getBorgInstallInfo(session);
//			if (!ii.isInstalled()) {
//				uploadBinary(session);
//				String cmd = String.format("chown root:root %s;chmod 755 %s", REMOTE_BORG_BINARY, REMOTE_BORG_BINARY);
//				SSHcommonUtil.runRemoteCommand(session, cmd);
//				ii = getBorgInstallInfo(session);
//				return FacadeResult.doneExpectedResult(ii, CommonActionResult.DONE);
//			} else {
//				return FacadeResult.doneExpectedResult(ii, CommonActionResult.PREVIOUSLY_DONE);
//			}
//		} catch (RunRemoteCommandException | IOException | ScpException e) {
//			ExceptionUtil.logErrorException(logger, e);
//			return FacadeResult.unexpectedResult(e);
//		}
//	}

//	public void uploadBinary(Session session) throws ScpException, IOException {
//		Path localPath = appSettings.getDownloadRoot().resolve(StringUtil.getLastPartOfUrl(BORG_BINARY_URL));
//		if (Files.exists(localPath)) {
//			ScpUtil.to(session, localPath.toString(), REMOTE_BORG_BINARY);
//		} else {
//			fileDownloader.download(BORG_BINARY_URL);
//		}
//	}

	public FacadeResult<RemoteCommandResult> initRepo(Session session, String repoPath) {
		try {
			if (!StringUtil.hasAnyNonBlankWord(repoPath)) {
				return FacadeResult.showMessageUnExpected(CommonMessageKeys.MALFORMED_VALUE, repoPath);
			}
			String command = String.format("borg init --encryption=none %s", repoPath);
			RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, command);
			if (rcr.isCommandNotFound()) {
				return FacadeResult.showMessageUnExpected(CommonMessageKeys.APPLICATION_NOTINSTALLED, command);
			}
			if (rcr.getExitValue() != 0) {
				boolean isFileNotFound = rcr.getAllTrimedNotEmptyLines().stream()
						.anyMatch(line -> line.contains("FileNotFoundError"));
				if (isFileNotFound) {
					String parentPath = RemotePathUtil.getParentWithEndingSlash(repoPath);
					rcr = SSHcommonUtil.runRemoteCommand(session, String.format("mkdir -p %s", parentPath));
					rcr = SSHcommonUtil.runRemoteCommand(session,
							String.format("borg init --encryption=none %s", repoPath));
				}
				
				boolean iserrorPath = rcr.getAllTrimedNotEmptyLines().stream().anyMatch(line -> line.contains("argument REPOSITORY: Invalid location format:"));
				if (iserrorPath) {
					return FacadeResult.showMessageUnExpected(CommonMessageKeys.MALFORMED_VALUE, repoPath);
				}
				
				//Repository /abc already exists.
				boolean alreadyExists = rcr.getAllTrimedNotEmptyLines().stream().anyMatch(line -> line.trim().matches("Repository .* already exists\\.") || line.contains("A repository already exists at"));
				
				if (alreadyExists) {
					return FacadeResult.showMessageUnExpected(CommonMessageKeys.OBJECT_ALREADY_EXISTS, repoPath);
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
		return server == null || server.getBorgDescription() == null || server.getBorgDescription().getIncludes() == null || server.getBorgDescription().getIncludes().isEmpty();
	}

	//@formatter:off
	public FacadeResult<BorgPruneResult> pruneRepo(Session session, Server server) {
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
	
	//@formatter:off
	@MeasureTimeCost
	public FacadeResult<BorgDownload> downloadRepo(Session session, Server server) {
		try {
			BorgDescription bd = server.getBorgDescription();
			String findCmd = String.format("find %s -type f -printf '%%s->%%p\n'", bd.getRepo());
			RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, findCmd);
			FileInAdirectory fid = new FileInAdirectory(rcr);

			String md5Cmd = String.format("md5sum %s", fid.getFileNamesSeparatedBySpace());

			rcr = SSHcommonUtil.runRemoteCommand(session, md5Cmd);
			List<String> md5s = rcr.getAllTrimedNotEmptyLines().stream().map(line -> line.split("\\s+", 2))
					.filter(ss -> ss.length == 2).map(ss -> ss[0]).collect(Collectors.toList());
			fid.setMd5s(md5s);

			final Path rRepo = Paths.get(bd.getRepo());
			final Path localRepo = appSettings.getBorgRepoDir(server);

			List<FileInfo> fis = fid.getFiles().stream().map(fi -> {
				fi.setLfileAbs(localRepo.resolve(rRepo.relativize(Paths.get(fi.getRfileAbs()))));
				return fi;
			}).collect(Collectors.toList());
			
			BorgDownload bdrs = new BorgDownload();
			bdrs.setTotalFiles(fis.size());
			long totalBytes = 0;
			long downloadBytes = 0;
			int downloadFiles = 0;
			for (FileInfo fi : fis) {
				if (Files.exists(fi.getLfileAbs())) {
					String m = Md5Checksum.getMD5Checksum(fi.getLfileAbs());
					if (m.equalsIgnoreCase(fi.getMd5())) {
						fi.setDownloaded(true);
						totalBytes += Files.size(fi.getLfileAbs());
						continue;
					}
				}
				SSHcommonUtil.downloadWithTmpDownloadingFile(session, fi.getRfileAbs(), fi.getLfileAbs());
				fi.setDownloaded(true);
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
					.collect(Collectors.toList());
			
			for(Path p : pathes) {
				Files.delete(p);
			}

			return FacadeResult.doneExpectedResult(bdrs, CommonActionResult.DONE);
		} catch (RunRemoteCommandException | IOException | ScpException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}

	}

	@Exclusive(TaskLocks.TASK_BORG)
	public FacadeResult<RemoteCommandResult> archive(Session session, Server server, String archiveNamePrefix, boolean solveProblems) {
//		if (solveProblems) {
//			install(session);
//			initRepo(session, server.getBorgDescription().getRepo());
//		}
		try {
			BorgDescription borgDescription = server.getBorgDescription();
			List<String> cmdparts = new ArrayList<>();
			cmdparts.add("borg create --stats --verbose --compression lz4 --exclude-caches");
			if (borgDescription.getExcludes() != null) {
				for (String f : borgDescription.getExcludes()) {
					cmdparts.add("--exclude");
					cmdparts.add("'" + f + "'");
				}
			}
			cmdparts.add(borgDescription.getRepo() + "::" + archiveNamePrefix
					+ new SimpleDateFormat(borgDescription.getArchiveFormat()).format(new Date()));
			for (String f : borgDescription.getIncludes()) {
				cmdparts.add(f);
			}
			String cmd = String.join(" ", cmdparts);
			RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, cmd);
			if (rcr.isCommandNotFound()) {
				throw new UnExpectedContentException(null, CommonMessageKeys.APPLICATION_NOTINSTALLED, cmd);
			}
			if (rcr.getExitValue() == 0) {
				return FacadeResult.doneExpectedResultDone(rcr);
			} else {
				List<String> lines = rcr.getAllTrimedNotEmptyLines();
				String errOut = rcr.getErrOut();
				if (errOut != null) {
					if (lines.stream().anyMatch(line -> line.contains("Need at least one PATH") || line.contains("the following arguments are required: PATH"))) {
						return FacadeResult.unexpectedResult(NO_INCLUDES);
					} else if (lines.stream().anyMatch(line -> line.trim().matches("Repository .* does not exist."))) {
						return FacadeResult.unexpectedResult(REPO_NON_INIT);
					} else {
						logger.error(rcr.getErrOut());
						return FacadeResult.unexpectedResult(UNKNOWN);
					}
				} else {
					return FacadeResult.unexpectedResult(UNKNOWN);
				}
			}
		} catch (RunRemoteCommandException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}
	}

	@Autowired
	public void setAppSettings(MyAppSettings appSettings) {
		this.appSettings = appSettings;
	}

	public FacadeResult<BorgListResult> listArchives(Session session, Server server) {
		try {
			BorgDescription bd = server.getBorgDescription();
			RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, String.format("borg list %s", bd.getRepo())); 
			return FacadeResult.doneExpectedResult(new BorgListResult(rcr), CommonActionResult.DONE);
		} catch (RunRemoteCommandException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}
	}
	
//	borg info /path/to/repo::2017-06-29T11:00-srv
	public FacadeResult<List<String>> archiveInfo(Session session, Server server, String archive) {
		try {
			BorgDescription bd = server.getBorgDescription();
			RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, String.format("borg info %s::%s", bd.getRepo(), archive));
			return FacadeResult.doneExpectedResult(rcr.getAllTrimedNotEmptyLines(), CommonActionResult.DONE);
		} catch (RunRemoteCommandException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}
	}

	public FacadeResult<RemoteCommandResult> listRepoFiles(Session session, Server server) {
		try {
			BorgDescription bd = server.getBorgDescription();
			RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, String.format("ls -lR %s", bd.getRepo()));
			return FacadeResult.doneExpectedResult(rcr, CommonActionResult.DONE);
		} catch (RunRemoteCommandException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}
	}

	@Autowired
	public void setFileDownloader(FileDownloader fileDownloader) {
		this.fileDownloader = fileDownloader;
	}
	
	

	//@formatter:on

	/**
	 * borg产生的卷是有时间顺序的，每个卷都有一个名称，所以可以恢复到各个时间点。prune之后，比如剩下7个天备份，4个月备份，那么恢复的粒度就不是每天了。
	 * @param session
	 * @param server
	 * @return
	 */
	public FacadeResult<?> archive(Session session, Server server) {
		BorgDescription bd = server.getBorgDescription();
		return archive(session, server, bd.getArchiveNamePrefix(), false);
	}

	public FacadeResult<RemoteCommandResult> archive(Session session, Server server, boolean solveProblems) {
		BorgDescription bd = server.getBorgDescription();
		return archive(session, server, bd.getArchiveNamePrefix(), solveProblems);
	}
}
