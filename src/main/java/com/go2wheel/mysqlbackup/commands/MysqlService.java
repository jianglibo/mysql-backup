package com.go2wheel.mysqlbackup.commands;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import com.go2wheel.mysqlbackup.exception.MysqlAccessDeniedException;
import com.go2wheel.mysqlbackup.exception.MysqlNotStartedException;
import com.go2wheel.mysqlbackup.exception.MysqlUnreadyException;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.exception.ScpException;
import com.go2wheel.mysqlbackup.expect.MysqlDumpExpect;
import com.go2wheel.mysqlbackup.expect.MysqlFlushLogExpect;
import com.go2wheel.mysqlbackup.model.MysqlDump;
import com.go2wheel.mysqlbackup.model.MysqlInstance;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.MysqlDumpService;
import com.go2wheel.mysqlbackup.service.MysqlInstanceService;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.util.FileUtil;
import com.go2wheel.mysqlbackup.util.MysqlUtil;
import com.go2wheel.mysqlbackup.util.PathUtil;
import com.go2wheel.mysqlbackup.util.RemotePathUtil;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.util.ScpUtil;
import com.go2wheel.mysqlbackup.util.TaskLocks;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.value.FacadeResult.CommonActionResult;
import com.go2wheel.mysqlbackup.value.LinuxLsl;
import com.go2wheel.mysqlbackup.value.LogBinSetting;
import com.go2wheel.mysqlbackup.value.MycnfFileHolder;
import com.go2wheel.mysqlbackup.value.ResultEnum;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@Service
public class MysqlService {

	public static final String ALREADY_DUMP = "mysql.dump.already";

	private Logger logger = LoggerFactory.getLogger(getClass());

	private MysqlUtil mysqlUtil;

	private MyAppSettings appSettings;
	
	@Autowired
	private MysqlDumpService mysqlDumpService;

	@Autowired
	private MysqlInstanceService mysqlInstanceService;

	@Autowired
	public void setMysqlUtil(MysqlUtil mysqlUtil) {
		this.mysqlUtil = mysqlUtil;
	}

	@Autowired
	public void setAppSettings(MyAppSettings appSettings) {
		this.appSettings = appSettings;
	}

	public FacadeResult<LinuxLsl> mysqlDump(Session session, Server server) {
		return mysqlDump(session, server, false);
	}

	private Path getDumpFile(Path dumpDir) {
		return dumpDir.resolve(Paths.get(MysqlUtil.DUMP_FILE_NAME).getFileName());
	}

	public boolean isMysqlNotReadyForBackup(Server server) {
		 return server == null || server.getMysqlInstance() == null || server.getMysqlInstance().getLogBinSetting() == null || server.getMysqlInstance().getLogBinSetting().isEmpty();
	}

	@Exclusive(TaskLocks.TASK_MYSQL)
	@MeasureTimeCost
	public FacadeResult<LinuxLsl> mysqlDump(Session session, Server server, boolean force) {
		try {
			Path dumpDir = appSettings.getDumpDir(server);
			Path logbinDir = appSettings.getLogBinDir(server);
			Path localDumpFile = getDumpFile(dumpDir);
			if (Files.exists(localDumpFile) && !force) {
				return saveDumpResult(server, FacadeResult.doneExpectedResultPreviousDone(ALREADY_DUMP));
			}
			if (force) {
				FileUtil.backup(3, false, dumpDir, logbinDir);
				Files.createDirectories(dumpDir);
				Files.createDirectories(logbinDir);
			}
			List<String> r = new MysqlDumpExpect(session, server).start();
			if (r.size() == 2) {
				LinuxLsl llsl = LinuxLsl.matchAndReturnLinuxLsl(r.get(0)).get();
				SSHcommonUtil.downloadWithTmpDownloadingFile(session, llsl.getFilename(), getDumpFile(dumpDir));
				return saveDumpResult(server, FacadeResult.doneExpectedResult(llsl, CommonActionResult.DONE));
			} else {
				return saveDumpResult(server, FacadeResult.unexpectedResult(r.get(0)));
			}
		} catch (IOException | RunRemoteCommandException | ScpException e) {
			ExceptionUtil.logErrorException(logger, e);
			return saveDumpResult(server, FacadeResult.unexpectedResult(e));
		}
		
	}
	
	private FacadeResult<LinuxLsl> saveDumpResult(Server server, FacadeResult<LinuxLsl> fr) {
		MysqlDump md = new MysqlDump();
		md.setCreatedAt(new Date());
		md.setTimeCost(fr.getEndTime() - fr.getStartTime());
		if (fr.isExpected()) {
			if (fr.getResult() != null) {
				md.setFileSize(fr.getResult().getSize());
				md.setResult(ResultEnum.SUCCESS);
			} else if (MysqlService.ALREADY_DUMP.equals(fr.getMessage())) {
				md.setResult(ResultEnum.SKIP);
			} else {
				md.setResult(ResultEnum.UNKNOWN);
			}
		} else {
			md.setResult(ResultEnum.UNKNOWN);
		}
		md.setServerId(server.getId());
		mysqlDumpService.save(md);
		return fr;
	}

	@Exclusive(TaskLocks.TASK_MYSQL)
	@MeasureTimeCost
	public FacadeResult<String> mysqlFlushLogs(Session session, Server server) {
		if (!server.getMysqlInstance().isReadyForBackup()) {
			throw new MysqlUnreadyException();
		}
		MysqlFlushLogExpect mfle = new MysqlFlushLogExpect(session, server);
		List<String> r = mfle.start();
		if (r.size() == 2) {
			return downloadBinLog(session, server);
		} else {
			return FacadeResult.unexpectedResult(r.get(0));
		}
	}

	// @formatter:off
	public FacadeResult<String> downloadBinLog(Session session, Server server) {
		try {
			LogBinSetting lbs = server.getMysqlInstance().getLogBinSetting();
			String remoteIndexFile = lbs.getLogBinIndex();
			String basenameOnlyName = lbs.getLogBinBasenameOnlyName();

			String binLogIndexOnlyName = lbs.getLogBinIndexNameOnly();

			Path localDir = appSettings.getLogBinDir(server);
			Path localIndexFile = localDir.resolve(binLogIndexOnlyName);

			if (Files.exists(localIndexFile)) {
				PathUtil.archiveLocalFile(localIndexFile, 6);
			}
			
			SSHcommonUtil.downloadWithTmpDownloadingFile(session, remoteIndexFile, localIndexFile);

			List<String> localBinLogFiles = Files.list(localDir)
					.map(p -> p.getFileName().toString())
					.collect(Collectors.toList());
			
			// index file contains all logbin file names.
			List<String> unLocalExists = Files.lines(localIndexFile)
					.filter(l -> l.indexOf(basenameOnlyName) != -1)
					.map(l -> l.trim())
					.map(l -> Paths.get(l).getFileName().toString())
					.filter(l -> !localBinLogFiles.contains(l))
					.collect(Collectors.toList());
			
			for(String fn : unLocalExists) {
				SSHcommonUtil.downloadWithTmpDownloadingFile(session, RemotePathUtil.getLogBinFile(server, fn),
						localDir.resolve(fn));

			}
			return FacadeResult.doneExpectedResult(localIndexFile.toAbsolutePath().toString(), CommonActionResult.DONE);
		} catch (RunRemoteCommandException | IOException | ScpException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}

	}
	
	
	public FacadeResult<?> disableLogbin(Session session, Server server) {
		try {

			MysqlInstance mi = server.getMysqlInstance();
			LogBinSetting lbs = mi.getLogBinSetting();
			try {
				lbs = mysqlUtil.getLogbinState(session, server);
			} catch (MysqlNotStartedException e) {
				mysqlUtil.restartMysql(session);
				lbs = mysqlUtil.getLogbinState(session, server);
			}
			
			MycnfFileHolder mfh = mysqlUtil.getMyCnfFile(session, server); // 找到起作用的my.cnf配置文件。
			String mycnfFile = mfh.getMyCnfFile();
			
			if (lbs.isEnabled()) {
				mfh.disableBinLog();
				SSHcommonUtil.backupFile(session, mycnfFile); // 先备份配置文件， my.cnf -> my.cnf.1
				ScpUtil.to(session, mycnfFile, mfh.toByteArray());
				mysqlUtil.restartMysql(session); // 重启Mysql
				lbs = mysqlUtil.getLogbinState(session, server); // 获取最新的logbin状态。
			}
			
			if (lbs.isEnabled()) {
				return FacadeResult.unexpectedResult("mysql.disablelogbin.failed");
			}
			mi.setMycnfFile(mycnfFile);
			mi.setLogBinSetting(lbs);
			mi = mysqlInstanceService.save(mi);
			server.setMysqlInstance(mi);
			return FacadeResult.doneExpectedResult(server, CommonActionResult.DONE);
		} catch (JSchException | IOException | RunRemoteCommandException | ScpException | MysqlAccessDeniedException | MysqlNotStartedException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}
	}

	public FacadeResult<?> enableLogbin(Session session, Server server, String logBinValue) {
		try {
			MysqlInstance mi = server.getMysqlInstance();
			LogBinSetting lbs = mi.getLogBinSetting();
			// always check the server for sure.
			try {
				lbs = mysqlUtil.getLogbinState(session, server);
			} catch (MysqlNotStartedException e) {
				mysqlUtil.restartMysql(session);
				lbs = mysqlUtil.getLogbinState(session, server);
			}
			
			MycnfFileHolder mfh = mysqlUtil.getMyCnfFile(session, server); // 找到起作用的my.cnf配置文件。
			String mycnfFile = mfh.getMyCnfFile();
			
			if (!lbs.isEnabled()) {
				mfh.enableBinLog(logBinValue); // 修改logbin的值
				SSHcommonUtil.backupFile(session, mycnfFile); // 先备份配置文件， my.cnf -> my.cnf.1
				ScpUtil.to(session, mycnfFile, mfh.toByteArray());
				mysqlUtil.restartMysql(session); // 重启Mysql
				lbs = mysqlUtil.getLogbinState(session, server); // 获取最新的logbin状态。
			}

			if (!lbs.isEnabled()) {
				return FacadeResult.unexpectedResult("mysql.enablelogbin.failed");
			}
			mi.setMycnfFile(mycnfFile);
			mi.setLogBinSetting(lbs);
			mi = mysqlInstanceService.save(mi);
			server.setMysqlInstance(mi);
			return FacadeResult.doneExpectedResult(server, CommonActionResult.DONE);
		} catch (JSchException | IOException | RunRemoteCommandException | ScpException | MysqlAccessDeniedException | MysqlNotStartedException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}
	}

//	public FacadeResult<?> updateMysqlDescription(Server server) {
//		try {
//			boxService.writeDescription(server);
//		} catch (IOException e) {
//			ExceptionUtil.logErrorException(logger, e);
//			FacadeResult.unexpectedResult(e);
//		}
//		return FacadeResult.doneExpectedResult(server, CommonActionResult.DONE);
//	}

//	public FacadeResult<?> updateMysqlDescription(Server server, String username, String password, int port,
//			String flushLogCron) {
//		MysqlInstance mi = server.getMysqlInstance();
//
//		mi.setUsername(username);
//		mi.setPassword(password);
//		mi.setPort(port);
//		if (!flushLogCron.equals(mi.getFlushLogCron())) {
//			mi.setFlushLogCron(flushLogCron);
//			CronExpressionChangeEvent cece = new CronExpressionChangeEvent(this, BoxUtil.getBorgPruneJobKey(server),
//					BoxUtil.getBorgPruneTriggerKey(server), flushLogCron);
//			applicationEventPublisher.publishEvent(cece);
//		}
//		
//		server.setMysqlInstance(mi);
//		return updateMysqlDescription(server);
//	}

	public FacadeResult<String> getMyCnfFile(Session session, Server server) {
		try {
			return FacadeResult.doneExpectedResult(mysqlUtil.getEffectiveMyCnf(session, server), CommonActionResult.DONE);
		} catch (RunRemoteCommandException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}
	}

}
