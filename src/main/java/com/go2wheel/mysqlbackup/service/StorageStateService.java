package com.go2wheel.mysqlbackup.service;

import java.io.IOException;
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

import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.model.Diskfree;
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

	public List<StorageState> getLinuxStorageState(Server server, Session session) {
		List<DiskFreeAllString> dfss = getDiskUsage(server, session);
		List<StorageState> dfs = dfss.stream().map(dd -> dd.toStorageState()).collect(Collectors.toList());
		final Date d = new Date();
		return dfs.stream().map(df -> {
			df.setCreatedAt(d);
			df.setServerId(server.getId());
			return storageStateDbService.save(df);
		}).collect(Collectors.toList());
	}

	public List<StorageState> getWinLocalDiskFree(Server server) throws IOException {
		String pscommand = "Get-PSDrive | Where-Object Name -Match '^.{1}$' | Format-List -Property *";
		ProcessExecResult rcr = PSUtil.runPsCommand(pscommand);
		List<Map<String, String>> lmss = PSUtil.parseFormatList(rcr.getStdOutFilterEmpty());
		List<StorageState> dfs = new ArrayList<>();
		final Date d = new Date();
		for (Map<String, String> mss : lmss) {
			StorageState df = new StorageState();
			df.setCreatedAt(d);
			df.setServerId(server.getId());
			long used = Long.parseLong(mss.get("Used"));
			long free = Long.parseLong(mss.get("Free"));
			df.setRoot(mss.get("Root"));
			df.setAvailable(free);
			df.setUsed(used);
			dfs.add(storageStateDbService.save(df));
		}
		return dfs;
	}

	private static class DiskFreeAllString {
		private String fileSystem = "";
		private String blocks = "";
		private String used = "";
		private String available = "";
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

		public Diskfree toDiskfree() {
			Diskfree df = new Diskfree();
			df.setFileSystem(fileSystem);
			df.setBlocks(StringUtil.parseInt(blocks));
			df.setMountedOn(mountedOn);
			df.setUsed(StringUtil.parseInt(used));
			df.setUsePercent(StringUtil.parseInt(use));
			df.setAvailable(StringUtil.parseInt(available));
			return df;
		}
	}
}
