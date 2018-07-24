package com.go2wheel.mysqlbackup.installer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.go2wheel.mysqlbackup.SettingsInDb;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.exception.ScpException;
import com.go2wheel.mysqlbackup.http.FileDownloader;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.Software;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.util.ScpUtil;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.go2wheel.mysqlbackup.value.FacadeResult.CommonActionResult;
import com.google.common.collect.Lists;
import com.jcraft.jsch.Session;

@Service
public class BorgInstaller extends InstallerBase<InstallInfo> {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private static final String BORG_BINARY_URL = "https://github.com/borgbackup/borg/releases/download/1.1.5/borg-linux64";
	
	private static final String REMOTE_BORG_BINARY = "/usr/local/bin/borg";
	
	@Autowired
	private SshSessionFactory sshSessionFactory;
	
	@Autowired
	private SettingsInDb settingsInDb;
	
	private Path downloadPath;
	
	@PostConstruct
	public void post() throws IOException {
		downloadPath = Paths.get(settingsInDb.getString("installer.download", "notingit/download"));
		if (!Files.exists(downloadPath)) {
			Files.createDirectories(downloadPath);
		}
		
		Software software = new Software();
		software.setName("BORG");
		software.setVersion("1.1.5");
		software.setTargetEnv("linux_centos");
		software.setDlurl(BORG_BINARY_URL);
		
		saveToDb(software);
	}
	
	public FacadeResult<InstallInfo> install(Session session, Server server, Software software, Map<String, String> parasMap) {
		BorgInstallInfo ii;
		try {
			ii = getBorgInstallInfo(session);
			if (!ii.isInstalled()) {
				uploadBinary(session);
				String cmd = String.format("chown root:root %s;chmod 755 %s", REMOTE_BORG_BINARY, REMOTE_BORG_BINARY);
				SSHcommonUtil.runRemoteCommand(session, cmd);
				ii = getBorgInstallInfo(session);
				return FacadeResult.doneExpectedResult(ii, CommonActionResult.DONE);
			} else {
				return FacadeResult.doneExpectedResult(ii, CommonActionResult.PREVIOUSLY_DONE);
			}
		} catch (RunRemoteCommandException | IOException | ScpException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}
	}

	@Override
	public FacadeResult<InstallInfo> install(Server server, Software software, Map<String, String> parasMap) {
		return null;
	}

	@Override
	public CompletableFuture<FacadeResult<InstallInfo>> installAsync(Server server, Software software,
			Map<String, String> parasMap) {
		return null;
	}

	@Override
	public boolean canHandle(Software software) {
		return false;
	}
	
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
	
	public void uploadBinary(Session session) throws ScpException, IOException {
		Path localPath = downloadPath.resolve(StringUtil.getLastPartOfUrl(BORG_BINARY_URL));
		if (Files.exists(localPath)) {
			ScpUtil.to(session, localPath.toString(), REMOTE_BORG_BINARY);
		} else {
			fileDownloader.download(BORG_BINARY_URL);
		}
	}

}
