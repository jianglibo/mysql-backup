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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.go2wheel.mysqlbackup.MyAppSettings;
import com.go2wheel.mysqlbackup.aop.Exclusive;
import com.go2wheel.mysqlbackup.commands.BoxService;
import com.go2wheel.mysqlbackup.event.CronExpressionChangeEvent;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.exception.ScpException;
import com.go2wheel.mysqlbackup.http.FileDownloader;
import com.go2wheel.mysqlbackup.util.BoxUtil;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.util.Md5Checksum;
import com.go2wheel.mysqlbackup.util.RemotePathUtil;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.util.ScpUtil;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.util.TaskLocks;
import com.go2wheel.mysqlbackup.value.BorgBackupDescription;
import com.go2wheel.mysqlbackup.value.BorgListResult;
import com.go2wheel.mysqlbackup.value.BorgPruneResult;
import com.go2wheel.mysqlbackup.value.Box;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.value.FacadeResult.CommonActionResult;
import com.go2wheel.mysqlbackup.value.FileInAdirectory;
import com.go2wheel.mysqlbackup.value.FileInfo;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.jcraft.jsch.Session;

@Service
public class BorgService {

	private Logger logger = LoggerFactory.getLogger(getClass());

	// https://borgbackup.readthedocs.io/en/stable/quickstart.html
	// sudo cp borg-linux64 /usr/local/bin/borg

	// sudo chown root:root /usr/local/bin/borg
	// sudo chmod 755 /usr/local/bin/borg
	// ln -s /usr/local/bin/borg /usr/local/bin/borgfs

	public static final String BORG_BINARY_URL = "https://github.com/borgbackup/borg/releases/download/1.1.5/borg-linux64";
	public static final String REMOTE_BORG_BINARY = "/usr/local/bin/borg";

	private MyAppSettings appSettings;

	private FileDownloader fileDownloader;

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;
	
	@Autowired
	private BoxService boxService;

	private InstallationInfo getInstallationInfo(Session session) throws RunRemoteCommandException {
		InstallationInfo ii = new InstallationInfo();
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

	public FacadeResult<InstallationInfo> unInstall(Session session) {
		try {
			InstallationInfo ii = getInstallationInfo(session);
			if (ii.isInstalled()) {
				SSHcommonUtil.deleteRemoteFile(session, REMOTE_BORG_BINARY);
				return FacadeResult.doneExpectedResult(getInstallationInfo(session), CommonActionResult.DONE);
			} else {
				return FacadeResult.doneExpectedResult(CommonActionResult.PREVIOUSLY_DONE);
			}
		} catch (RunRemoteCommandException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}
	}

	public FacadeResult<InstallationInfo> install(Session session) {
		InstallationInfo ii;
		try {
			ii = getInstallationInfo(session);
			if (!ii.isInstalled()) {
				uploadBinary(session);
				String cmd = String.format("chown root:root %s;chmod 755 %s", REMOTE_BORG_BINARY, REMOTE_BORG_BINARY);
				SSHcommonUtil.runRemoteCommand(session, cmd);
				ii = getInstallationInfo(session);
				return FacadeResult.doneExpectedResult();
			} else {
				return FacadeResult.doneExpectedResult(CommonActionResult.PREVIOUSLY_DONE);
			}
		} catch (RunRemoteCommandException | IOException | ScpException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}
	}

	public void uploadBinary(Session session) throws ScpException, IOException {
		Path localPath = appSettings.getDownloadRoot().resolve(StringUtil.getLastPartOfUrl(BORG_BINARY_URL));
		if (Files.exists(localPath)) {
			ScpUtil.to(session, localPath.toString(), REMOTE_BORG_BINARY);
		} else {
			fileDownloader.download(BORG_BINARY_URL);
		}
	}

	public FacadeResult<RemoteCommandResult> initRepo(Session session, String repoPath) {
		try {
			RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session,
					String.format("borg init --encryption=none %s", repoPath));
			if (rcr.isCommandNotFound()) {
				return FacadeResult.unexpectedResult("command not found.");
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
			}
			return FacadeResult.doneExpectedResult(rcr, CommonActionResult.DONE);
		} catch (RunRemoteCommandException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}
	}

	//@formatter:off
	public FacadeResult<BorgPruneResult> pruneRepo(Session session, Box box) {
		try {
			String cmd = String.format("borg prune --list --verbose --prefix %s --show-rc --keep-daily %s --keep-weekly %s --keep-monthly %s %s",
					box.getBorgBackup().getArchiveNamePrefix(), 7, 4, 6,
					box.getBorgBackup().getRepo());
			return FacadeResult.doneExpectedResult(new BorgPruneResult(SSHcommonUtil.runRemoteCommand(session, cmd)), CommonActionResult.DONE);
		} catch (RunRemoteCommandException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}
	}
	
	//@formatter:off
	public FacadeResult<?> downloadRepo(Session session, Box box) {
		try {
			String findCmd = String.format("find %s -type f -printf '%%s->%%p\n'", box.getBorgBackup().getRepo());
			RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, findCmd);
			FileInAdirectory fid = new FileInAdirectory(rcr);

			String md5Cmd = String.format("md5sum %s", fid.getFileNamesSeparatedBySpace());

			rcr = SSHcommonUtil.runRemoteCommand(session, md5Cmd);
			List<String> md5s = rcr.getAllTrimedNotEmptyLines().stream().map(line -> line.split("\\s+", 2))
					.filter(ss -> ss.length == 2).map(ss -> ss[0]).collect(Collectors.toList());
			fid.setMd5s(md5s);

			final Path rRepo = Paths.get(box.getBorgBackup().getRepo());
			final Path localRepo = appSettings.getBorgRepoDir(box);

			List<FileInfo> fis = fid.getFiles().stream().map(fi -> {
				fi.setLfileAbs(localRepo.resolve(rRepo.relativize(Paths.get(fi.getRfileAbs()))));
				return fi;
			}).collect(Collectors.toList());

			for (FileInfo fi : fis) {
				if (Files.exists(fi.getLfileAbs())) {
					String m = Md5Checksum.getMD5Checksum(fi.getLfileAbs());
					if (m.equalsIgnoreCase(fi.getMd5())) {
						fi.setDownloaded(true);
						continue;
					}
				}
				SSHcommonUtil.downloadWithTmpDownloadingFile(session, fi.getRfileAbs(), fi.getLfileAbs());
				fi.setDownloaded(true);
			}

			// delete the files already deleted from remote repo.
			List<Path> pathes = Files.walk(localRepo)
					.filter(p -> Files.isRegularFile(p))
					.filter(p -> !fid.fileExists(p))
					.collect(Collectors.toList());
			
			for(Path p : pathes) {
				Files.delete(p);
			}
			return FacadeResult.doneExpectedResult();
		} catch (RunRemoteCommandException | IOException | ScpException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}

	}

	@Exclusive(TaskLocks.TASK_BORG)
	public FacadeResult<RemoteCommandResult> archive(Session session, Box box, String archiveNamePrefix) {
		try {
			BorgBackupDescription borgDescription = box.getBorgBackup();
			List<String> cmdparts = new ArrayList<>();
			cmdparts.add("borg create --stats --verbose --compression lz4 --exclude-caches");
			for (String f : borgDescription.getExcludes()) {
				cmdparts.add("--exclude");
				cmdparts.add("'" + f + "'");
			}
			cmdparts.add(borgDescription.getRepo() + "::" + archiveNamePrefix
					+ new SimpleDateFormat(borgDescription.getArchiveFormat()).format(new Date()));
			for (String f : borgDescription.getIncludes()) {
				cmdparts.add(f);
			}
			String cmd = String.join(" ", cmdparts);
			return FacadeResult.doneExpectedResult(SSHcommonUtil.runRemoteCommand(session, cmd), CommonActionResult.DONE);
		} catch (RunRemoteCommandException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}
	}

	@Autowired
	public void setAppSettings(MyAppSettings appSettings) {
		this.appSettings = appSettings;
	}

	public FacadeResult<BorgListResult> listArchives(Session session, Box box) {
		try {
			return FacadeResult.doneExpectedResult( new BorgListResult(
					SSHcommonUtil.runRemoteCommand(session, String.format("borg list %s", box.getBorgBackup().getRepo()))), CommonActionResult.DONE);
		} catch (RunRemoteCommandException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}
	}

	public FacadeResult<RemoteCommandResult> listRepoFiles(Session session, Box box) {
		try {
			RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, String.format("ls -lR %s", box.getBorgBackup().getRepo()));
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
	
	
	public static class InstallationInfo {
		
		private boolean installed;
		private String executable;
		private String version;
		
		
		public static InstallationInfo notInstalled() {
			InstallationInfo ii = new InstallationInfo();
			ii.setInstalled(false);
			return ii;
		}
		
		public boolean isInstalled() {
			return installed;
		}
		public void setInstalled(boolean installed) {
			this.installed = installed;
		}
		public String getExecutable() {
			return executable;
		}
		public void setExecutable(String executable) {
			this.executable = executable;
		}
		public String getVersion() {
			return version;
		}
		public void setVersion(String version) {
			this.version = version;
		}

	}
	//@formatter:on
	public FacadeResult<?> updateBorgDescription(Session session, Box box, String repo, String archiveFormat,
			String archiveNamePrefix, String archiveCron, String pruneCron) {
		BorgBackupDescription bbdi = box.getBorgBackup();
		if (bbdi == null)
			bbdi = new BorgBackupDescription();

		if (!repo.isEmpty())
			bbdi.setRepo(repo);
		if (!archiveFormat.isEmpty())
			bbdi.setArchiveFormat(archiveFormat);
		if (!archiveNamePrefix.isEmpty())
			bbdi.setArchiveNamePrefix(archiveNamePrefix);
		if (!archiveCron.isEmpty()) {
			if (!archiveCron.equals(bbdi.getArchiveCron())) {
				bbdi.setArchiveCron(archiveCron);
				CronExpressionChangeEvent cece = new CronExpressionChangeEvent(this, BoxUtil.getBorgArchiveJobKey(box),
						BoxUtil.getBorgArchiveTriggerKey(box), archiveCron);
				applicationEventPublisher.publishEvent(cece);
			}
		}
		if (!pruneCron.isEmpty()) {
			if (!pruneCron.equals(bbdi.getPruneCron())) {
				bbdi.setPruneCron(pruneCron);
				CronExpressionChangeEvent cece = new CronExpressionChangeEvent(this, BoxUtil.getBorgPruneJobKey(box),
						BoxUtil.getBorgPruneTriggerKey(box), pruneCron);
				applicationEventPublisher.publishEvent(cece);
			}

		}
		box.setBorgBackup(bbdi);
		return saveBox(box);
	}

	public FacadeResult<?> updateBorgDescription(Session session, Box box, String include, String exclude,
			boolean isadd) {
		BorgBackupDescription bbdi = box.getBorgBackup();
		if (isadd) {
			if (!include.isEmpty())
				bbdi.getIncludes().add(include);
			if (!exclude.isEmpty())
				bbdi.getExcludes().add(exclude);
		} else {
			if (!include.isEmpty()) {
				bbdi.getIncludes().remove(include);
			}
			if (!exclude.isEmpty()) {
				bbdi.getExcludes().remove(exclude);
			}
		}
		FacadeResult<?> frb = saveBox(box);
		if (frb.getResult() != null) {
			return FacadeResult.doneExpectedResult(box.getBorgBackup(), CommonActionResult.DONE);
		} else {
			return frb;
		}
	}

	private FacadeResult<Box> saveBox(Box box) {
		try {
			boxService.writeDescription(box);
			return FacadeResult.doneExpectedResult(box, CommonActionResult.DONE);
		} catch (IOException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}
	}
}
