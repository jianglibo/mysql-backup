package com.go2wheel.mysqlbackup.borg;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.MyAppSettings;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.exception.ScpToException;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.util.PathUtil;
import com.go2wheel.mysqlbackup.util.RemotePathUtil;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.util.ScpUtil;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.value.BorgBackupDescription;
import com.go2wheel.mysqlbackup.value.InstallationInfo;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.jcraft.jsch.Session;

@Component
public class BorgTaskFacade {

	private Logger logger = LoggerFactory.getLogger(getClass());

	// https://borgbackup.readthedocs.io/en/stable/quickstart.html
	// sudo cp borg-linux64 /usr/local/bin/borg
	// sudo chown root:root /usr/local/bin/borg
	// sudo chmod 755 /usr/local/bin/borg
	// ln -s /usr/local/bin/borg /usr/local/bin/borgfs

	public static final String BORG_BINARY = "borg-linux64";
	public static final String REMOTE_BORG_BINARY = "/usr/local/bin/borg";

	private MyAppSettings appSettings;

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

	public InstallationInfo unInstall(Session session) throws RunRemoteCommandException {
		InstallationInfo ii = getInstallationInfo(session);
		if (ii.isInstalled()) {
			SSHcommonUtil.deleteRemoteFile(session, REMOTE_BORG_BINARY);
			return getInstallationInfo(session);
		} else {
			return ii;
		}
	}

	public InstallationInfo install(Session session) throws RunRemoteCommandException {
		InstallationInfo ii = getInstallationInfo(session);
		if (!ii.isInstalled()) {
			try {
				uploadBinary(session);
				String cmd = String.format("chown root:root %s;chmod 755 %s", REMOTE_BORG_BINARY, REMOTE_BORG_BINARY);
				SSHcommonUtil.runRemoteCommand(session, cmd);
				return getInstallationInfo(session);
			} catch (ScpToException e) {
				ii.setFailReason(e.getMessage());
				ii.setInstalled(false);
				ExceptionUtil.logErrorException(logger, e);
			}
		}
		return ii;
	}

	public void uploadBinary(Session session) throws ScpToException {
		Path localPath = appSettings.getDownloadRoot().resolve(BORG_BINARY);
		if (Files.exists(localPath)) {
			ScpUtil.to(session, localPath.toString(), REMOTE_BORG_BINARY);
		} else {
			throw new ScpToException(localPath.toString(), REMOTE_BORG_BINARY, "local file doesn't exists.");
		}
	}
	
	public void initRepo(Session session, String repoPath) throws RunRemoteCommandException {
		RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, String.format("borg init --encryption=none %s", repoPath));
		if (rcr.getExitValue() != 0) {
			boolean isFileNotFound = rcr.getAllTrimedNotEmptyLines().stream().anyMatch(line -> line.contains("FileNotFoundError"));
			if (isFileNotFound) {
				String parentPath = RemotePathUtil.getParentWithEndingSlash(repoPath);
				rcr = SSHcommonUtil.runRemoteCommand(session, String.format("mkdir -p %s", parentPath));
				rcr = SSHcommonUtil.runRemoteCommand(session, String.format("borg init --encryption=none %s", repoPath));
			}
		}
	}
	
	public  void archive(Session session, BorgBackupDescription borgDescription, String archiveName) {
		//	borg create /borg/repos/trepo::Monday /etc
	}

	@Autowired
	public void setAppSettings(MyAppSettings appSettings) {
		this.appSettings = appSettings;
	}
}
