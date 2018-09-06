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
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.Software;
import com.go2wheel.mysqlbackup.model.SoftwareInstallation;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.value.AsyncTaskValue;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.value.FacadeResult.CommonActionResult;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@Service
public class PowershellInstaller extends InstallerBase<InstallInfo> {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@PostConstruct
	public void post() throws IOException {
		syncToDb();
	}

	public FacadeResult<InstallInfo> unInstall(Session session, Server server, Software software) throws JSchException, IOException {
		try {
			SSHcommonUtil.runRemoteCommand(session, "yum install -y powershell");
			return FacadeResult.doneExpectedResult(getInstallInfo(session), CommonActionResult.DONE);
		} catch (RunRemoteCommandException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}
	}

	public FacadeResult<InstallInfo> install(Session session, Server server, Software software,
			Map<String, String> parasMap) throws JSchException, IOException {
		PowershellInstallInfo ii;
		try {
			ii = getInstallInfo(session);
			if (!ii.isInstalled()) {
				SSHcommonUtil.runRemoteCommand(session, "curl https://packages.microsoft.com/config/rhel/7/prod.repo | tee /etc/yum.repos.d/microsoft.repo");
				SSHcommonUtil.runRemoteCommand(session, "yum install -y powershell");
				
				ii = getInstallInfo(session);
				if (ii.isInstalled()) {
					SoftwareInstallation si = SoftwareInstallation.newInstance(server, software);
					softwareInstallationDbService.save(si);
					return FacadeResult.doneExpectedResult(ii, CommonActionResult.DONE);
				} else {
					return FacadeResult.unexpectedResult("unknown");
				}
			} else {
				SoftwareInstallation si = softwareInstallationDbService.findByServerAndSoftware(server, software);
				if (si == null) {
					si = SoftwareInstallation.newInstance(server, software);
				}
				softwareInstallationDbService.save(si);
				return FacadeResult.doneExpectedResult(ii, CommonActionResult.PREVIOUSLY_DONE);
			}
		} catch (RunRemoteCommandException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}
	}

	@Override
	public FacadeResult<InstallInfo> install(Server server, Software software, Map<String, String> parasMap)
			throws JSchException, IOException {
		return install(getSession(server), server, software, parasMap);
	}

	@Override
	public CompletableFuture<AsyncTaskValue> installAsync(Server server, Software software, String msgkey, Long id,
			Map<String, String> parasMap) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return new AsyncTaskValue(id, install(server, software, parasMap)).withDescription(msgkey);
			} catch (JSchException | IOException e) {
				return new AsyncTaskValue(id, FacadeResult.unexpectedResult(e)).withDescription(msgkey);
			}
		});
	}

	@Override
	public boolean canHandle(Software software) {
		return software.getName().equals("POWERSHELL");
	}

	private PowershellInstallInfo getInstallInfo(Session session) throws RunRemoteCommandException, JSchException, IOException {
		PowershellInstallInfo ii = new PowershellInstallInfo();
		RemoteCommandResult rcr;
		rcr = SSHcommonUtil.runRemoteCommand(session, "which pwsh;pwsh -v");
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
		Software software = new Software();
		software.setName("POWERSHELL");
		software.setVersion("ANY");
		software.setTargetEnv("linux_centos");
		software.setDlurl("");
		software.setInstaller("");
		saveToDb(software);
	}

	@Override
	public FacadeResult<InstallInfo> uninstall(Server server, Software software) throws JSchException, IOException {
		return unInstall(getSession(server), server, software);
	}

	@Override
	public CompletableFuture<AsyncTaskValue> uninstallAsync(Server server, Software software, String msgkey, Long id) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return new AsyncTaskValue(id, uninstall(server, software)).withDescription(msgkey);
			} catch (JSchException | IOException e) {
				return new AsyncTaskValue(id, FacadeResult.unexpectedResult(e)).withDescription(msgkey);
			}
		});
	}

	@Override
	public String getDescriptionMessageKey() {
		return "taskkey.installpowershell";
	}
}
