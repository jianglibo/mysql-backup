package com.go2wheel.mysqlbackup.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.model.Diskfree;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.value.DiskFreeAllString;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.jcraft.jsch.Session;

@Service
public class DiskfreeService {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private DiskfreeDbService diskfreeDbService;

	private List<DiskFreeAllString> getDiskUsage(Session session) {
		String command = "df -l -Bm";
		try {
			RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, command);
			return rcr.getAllTrimedNotEmptyLines().stream().map(DiskFreeAllString::build).filter(Objects::nonNull)
					.collect(Collectors.toList());
		} catch (RunRemoteCommandException e) {
			ExceptionUtil.logErrorException(logger, e);
		}
		return new ArrayList<>();
	}

	public List<Diskfree> getLinuxDiskfree(Server server, Session session) {
		List<DiskFreeAllString> dfss = getDiskUsage(session);
		List<Diskfree> dfs = dfss.stream().map(dd -> dd.toDiskfree()).collect(Collectors.toList());
		final Date d = new Date();
		return dfs.stream().map(df -> {
			df.setCreatedAt(d);
			df.setServerId(server.getId());
			return diskfreeDbService.save(df);
		}).collect(Collectors.toList());
	}
	
	public Diskfree getWinLocalDiskFree() {
		String pscommand = "Get-PSDrive | Where-Object Name -Match '^.{1}$' | Format-List -Property *";
		return null;
	}
}
