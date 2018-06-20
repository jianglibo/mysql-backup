package com.go2wheel.mysqlbackup.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.go2wheel.mysqlbackup.MyAppSettings;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.StorageState;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.util.PSUtil;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.value.ProcessExecResult;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.jcraft.jsch.Session;

@Service
public class StorageStateService {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private MyAppSettings myAppSettings;

	@Autowired
	private StorageStateDbService storageStateDbService;

	private List<DiskFreeAllString> getDiskUsage(Server server, Session session) {
		String command = "df -l";
		try {
			RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, command);
			return rcr.getAllTrimedNotEmptyLines().stream().map(DiskFreeAllString::build).filter(Objects::nonNull)
					.filter(dfas -> !"tmpfs".equals(dfas.fileSystem)).collect(Collectors.toList());
		} catch (RunRemoteCommandException e) {
			ExceptionUtil.logErrorException(logger, e);
		}
		return new ArrayList<>();
	}
	
	public List<StorageState> getStorageState(Server server, Session session) {
		List<StorageState> lss;
		if ("localhost".equals(server.getHost())) {
			lss = getWinLocalDiskFree(server);
		} else {
			lss = getLinuxStorageState(server, session);	
		}
		return lss;
		
	}

	private List<StorageState> getLinuxStorageState(Server server, Session session) {
		List<DiskFreeAllString> dfss = getDiskUsage(server, session);
		List<StorageState> dfs = dfss.stream().map(dd -> dd.toStorageState()).filter(it -> !myAppSettings.getStorageExcludes().contains(it.getRoot())).collect(Collectors.toList());
		final Date d = new Date();
		return dfs.stream().map(df -> {
			df.setCreatedAt(d);
			df.setServerId(server.getId());
			return storageStateDbService.save(df);
		}).collect(Collectors.toList());
	}

	private List<StorageState> getWinLocalDiskFree(Server server) {
		String pscommand = "Get-PSDrive | Where-Object Name -Match '^.{1}$' | Format-List -Property *";
		ProcessExecResult rcr = PSUtil.runPsCommand(pscommand);
		List<Map<String, String>> lmss = PSUtil.parseFormatList(rcr.getStdOutFilterEmpty());
		List<StorageState> dfs = new ArrayList<>();
		final Date d = new Date();
		for (Map<String, String> mss : lmss) {
			try {
				String root = mss.get("Root");
				if (myAppSettings.getStorageExcludes().contains(root))continue;
				String usedNumber = mss.get("Used");
				String freeNumber = mss.get("Free");
				if (usedNumber.trim().isEmpty() || freeNumber.trim().isEmpty())continue;
				StorageState df = new StorageState();
				df.setCreatedAt(d);
				df.setServerId(server.getId());
				long used = Long.parseLong(usedNumber);
				long free = Long.parseLong(freeNumber);
				df.setRoot(root);
				df.setAvailable(free);
				df.setUsed(used);
				dfs.add(storageStateDbService.save(df));
			} catch (NumberFormatException e) {
				ExceptionUtil.logErrorException(logger, e);
			}
		}
		return dfs;
	}

	private static class DiskFreeAllString {
		private String fileSystem = "";
		@SuppressWarnings("unused")
		private String blocks = "";
		private String used = "";
		private String available = "";
		@SuppressWarnings("unused")
		private String use = "";
		private String mountedOn = "";

		private DiskFreeAllString() {
		}

		public static DiskFreeAllString build(String line) {
			if (line.contains("Use%"))
				return null;
			String[] ss = line.trim().split("\\s+");
			if (ss.length == 6) {
				DiskFreeAllString du = new DiskFreeAllString();
				du.fileSystem = ss[0];
				du.blocks = ss[1];
				du.used = ss[2];
				du.available = ss[3];
				du.use = ss[4];
				du.mountedOn = ss[5];
				return du;
			}
			return null;
		}

		public StorageState toStorageState() {
			StorageState df = new StorageState();
			df.setRoot(mountedOn);
			df.setUsed(StringUtil.parseLong(used) * 1024);
			df.setAvailable(StringUtil.parseLong(available) * 1024);
			return df;
		}
	}

	public int pruneStorageState(Server server, int keepDays) {
		return storageStateDbService.remove(server, keepDays);
	}
}
