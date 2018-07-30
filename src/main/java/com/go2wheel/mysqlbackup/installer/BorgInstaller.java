package com.go2wheel.mysqlbackup.installer;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.exception.ScpException;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.Software;
import com.go2wheel.mysqlbackup.model.SoftwareInstallation;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.util.ScpUtil;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.value.FacadeResult.CommonActionResult;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.google.common.collect.Lists;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@Service
public class BorgInstaller extends InstallerBase<InstallInfo> {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private static final String REMOTE_BORG_BINARY_KEY = "remote-borg-binary";
	
	@PostConstruct
	public void post() throws IOException {
		syncToDb();
	}
	
	public FacadeResult<InstallInfo> unInstall(Session session, Server server, Software software) {
		try {
			BorgInstallInfo ii = getBorgInstallInfo(session);
			String rbb = software.getSettingsMap().get(REMOTE_BORG_BINARY_KEY);
			
			if (ii.isInstalled()) {
				SSHcommonUtil.deleteRemoteFile(session, rbb);
				removeInstallationInDb(server, software);
				return FacadeResult.doneExpectedResult(getBorgInstallInfo(session), CommonActionResult.DONE);
			} else {
				return FacadeResult.doneExpectedResult(ii, CommonActionResult.PREVIOUSLY_DONE);
			}
		} catch (RunRemoteCommandException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}
	}
	
	
	public FacadeResult<InstallInfo> install(Session session, Server server, Software software, Map<String, String> parasMap) {
		BorgInstallInfo ii;
		try {
			String rbb = null;
			if (parasMap != null && parasMap.containsKey(REMOTE_BORG_BINARY_KEY)) {
				String v = parasMap.get(REMOTE_BORG_BINARY_KEY);
				if (v.trim().length() > 0) {
					rbb = v.trim();
				}
			}
			if (rbb == null) {
				rbb = software.getSettingsMap().get(REMOTE_BORG_BINARY_KEY);
			}
			ii = getBorgInstallInfo(session);
			
			if (!ii.isInstalled()) {
				ScpUtil.to(session, getLocalInstallerPath(software).toString(), rbb);
				String cmd = String.format("chown root:root %s;chmod 755 %s", rbb, rbb);
				SSHcommonUtil.runRemoteCommand(session, cmd);
				ii = getBorgInstallInfo(session);
				if (ii.isInstalled()) {
					SoftwareInstallation si = SoftwareInstallation.newInstance(server, software).addSetting(REMOTE_BORG_BINARY_KEY, rbb);
					softwareInstallationDbService.save(si);
					return FacadeResult.doneExpectedResult(ii, CommonActionResult.DONE);
				} else {
					return FacadeResult.unexpectedResult("unknown");
				}
			} else {
					SoftwareInstallation si = softwareInstallationDbService.findByServerAndSoftware(server, software);
					if (si == null) {
						si = SoftwareInstallation.newInstance(server, software).addSetting(REMOTE_BORG_BINARY_KEY, rbb);
					}
					softwareInstallationDbService.save(si);
				return FacadeResult.doneExpectedResult(ii, CommonActionResult.PREVIOUSLY_DONE);
			}
		} catch (RunRemoteCommandException | ScpException | IOException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}
	}

	@Override
	public FacadeResult<InstallInfo> install(Server server, Software software, Map<String, String> parasMap) throws JSchException {
		return install(getSession(server), server, software, parasMap);
	}

	@Override
	public CompletableFuture<FacadeResult<InstallInfo>> installAsync(Server server, Software software,
			Map<String, String> parasMap) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return install(server, software, parasMap);
			} catch (JSchException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return FacadeResult.unexpectedResult(e);
			}
		});
	}

	@Override
	public boolean canHandle(Software software) {
		return software.getName().equals("BORG");
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

	@Override
	public void syncToDb() {
		try {
			Software software = new Software();
			software.setName("BORG");
			software.setVersion("1.1.5");
			software.setTargetEnv("linux_centos");
			software.setDlurl("https://github.com/borgbackup/borg/releases/download/1.1.5/borg-linux64");
			software.setInstaller("borg-linux64-1.1.5");
			List<String> settings = Lists.newArrayList();
			settings.add(REMOTE_BORG_BINARY_KEY + "=/usr/local/bin/borg");
			software.setSettings(settings);
			saveToDb(software);
			fileDownloader.downloadAsync(software.getDlurl(), settingsInDb.getDownloadPath().resolve(software.getInstaller()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public FacadeResult<InstallInfo> uninstall(Server server, Software software) throws JSchException {
		return unInstall(getSession(server), server, software);
	}

	@Override
	public CompletableFuture<FacadeResult<InstallInfo>> uninstallAsync(Server server, Software software) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return uninstall(server, software);
			} catch (JSchException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return FacadeResult.unexpectedResult(e);
			}
		});
	}

}
