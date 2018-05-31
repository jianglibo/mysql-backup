package com.go2wheel.mysqlbackup.commands;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.go2wheel.mysqlbackup.MyAppSettings;
import com.go2wheel.mysqlbackup.aop.Exclusive;
import com.go2wheel.mysqlbackup.aop.MeasureTimeCost;
import com.go2wheel.mysqlbackup.event.CronExpressionChangeEvent;
import com.go2wheel.mysqlbackup.exception.MysqlAccessDeniedException;
import com.go2wheel.mysqlbackup.exception.MysqlNotStartedException;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.exception.ScpException;
import com.go2wheel.mysqlbackup.expect.MysqlDumpExpect;
import com.go2wheel.mysqlbackup.expect.MysqlFlushLogExpect;
import com.go2wheel.mysqlbackup.util.BoxUtil;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.util.FileUtil;
import com.go2wheel.mysqlbackup.util.MysqlUtil;
import com.go2wheel.mysqlbackup.util.PathUtil;
import com.go2wheel.mysqlbackup.util.RemotePathUtil;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.util.ScpUtil;
import com.go2wheel.mysqlbackup.util.TaskLocks;
import com.go2wheel.mysqlbackup.value.Box;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.value.FacadeResult.CommonActionResult;
import com.go2wheel.mysqlbackup.value.LinuxLsl;
import com.go2wheel.mysqlbackup.value.LogBinSetting;
import com.go2wheel.mysqlbackup.value.MycnfFileHolder;
import com.go2wheel.mysqlbackup.value.MysqlInstance;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@Service
public class MysqlService {
	
	public static final String ALREADY_DUMP = "mysql.dump.already";

	private Logger logger = LoggerFactory.getLogger(getClass());

	private MysqlUtil mysqlUtil;

	private MyAppSettings appSettings;
	
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;
	
	@Autowired
	private BoxService boxService;

	@Autowired
	public void setMysqlUtil(MysqlUtil mysqlUtil) {
		this.mysqlUtil = mysqlUtil;
	}

	@Autowired
	public void setAppSettings(MyAppSettings appSettings) {
		this.appSettings = appSettings;
	}

	public FacadeResult<LinuxLsl> mysqlDump(Session session, Box box) {
		return mysqlDump(session, box, false);
	}

	private Path getDumpFile(Path dumpDir) {
		return dumpDir.resolve(Paths.get(MysqlUtil.DUMP_FILE_NAME).getFileName());
	}
	
	public boolean isMysqlNotReadyForBackup(Box box) {
		return box == null || box.getMysqlInstance() == null || box.getMysqlInstance().getLogBinSetting() == null;
	}

	@Exclusive(TaskLocks.TASK_MYSQL)
	@MeasureTimeCost
	public FacadeResult<LinuxLsl> mysqlDump(Session session, Box box, boolean force) {
		try {
			Path dumpDir = appSettings.getDumpDir(box);
			Path logbinDir = appSettings.getLogBinDir(box);
			Path localDumpFile = getDumpFile(dumpDir);
			if (Files.exists(localDumpFile) && !force) {
				return FacadeResult.doneExpectedResultPreviousDone(ALREADY_DUMP);
			}
			if (force) {
				FileUtil.backup(3, false, dumpDir, logbinDir);
				Files.createDirectories(dumpDir);
				Files.createDirectories(logbinDir);
			}
			List<String> r = new MysqlDumpExpect(session, box).start();
			if (r.size() == 2) {
				LinuxLsl llsl = LinuxLsl.matchAndReturnLinuxLsl(r.get(0)).get();
				SSHcommonUtil.downloadWithTmpDownloadingFile(session, llsl.getFilename(), getDumpFile(dumpDir));
				return FacadeResult.doneExpectedResult(llsl, CommonActionResult.DONE);
			} else {
				return FacadeResult.unexpectedResult(r.get(0));
			}
		} catch (IOException | RunRemoteCommandException | ScpException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}
	}

	@Exclusive(TaskLocks.TASK_MYSQL)
	@MeasureTimeCost
	public FacadeResult<String> mysqlFlushLogs(Session session, Box box) {
		MysqlFlushLogExpect mfle = new MysqlFlushLogExpect(session, box);
		List<String> r = mfle.start();
		if (r.size() == 2) {
			return downloadBinLog(session, box);
		} else {
			return FacadeResult.unexpectedResult(r.get(0));
		}
	}

	// @formatter:off
	public FacadeResult<String> downloadBinLog(Session session, Box box) {
		try {
			LogBinSetting lbs = box.getMysqlInstance().getLogBinSetting();
			String remoteIndexFile = lbs.getLogBinIndex();
			String basenameOnlyName = lbs.getLogBinBasenameOnlyName();

			String binLogIndexOnlyName = lbs.getLogBinIndexNameOnly();

			Path localDir = appSettings.getLogBinDir(box);
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
				SSHcommonUtil.downloadWithTmpDownloadingFile(session, RemotePathUtil.getLogBinFile(box, fn),
						localDir.resolve(fn));

			}
			return FacadeResult.doneExpectedResult(localIndexFile.toAbsolutePath().toString(), CommonActionResult.DONE);
		} catch (RunRemoteCommandException | IOException | ScpException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}

	}

	public FacadeResult<?> mysqlEnableLogbin(Session session, Box box, String logBinValue) {
		try {
			LogBinSetting lbs = box.getMysqlInstance().getLogBinSetting();
			if (lbs != null && lbs.isEnabled()) {
				return FacadeResult.doneExpectedResult(box, CommonActionResult.PREVIOUSLY_DONE);
			} else {
				try {
					lbs = mysqlUtil.getLogbinState(session, box);
				} catch (MysqlNotStartedException e) {
					mysqlUtil.restartMysql(session);
					lbs = mysqlUtil.getLogbinState(session, box);
				}
				if (lbs.isEnabled()) {
					box.getMysqlInstance().setLogBinSetting(lbs);
					boxService.writeDescription(box);
					return FacadeResult.doneExpectedResult(box, CommonActionResult.PREVIOUSLY_DONE);
				} else {
					MycnfFileHolder mfh = mysqlUtil.getMyCnfFile(session, box); // 找到起作用的my.cnf配置文件。
					String mycnfFile = box.getMysqlInstance().getMycnfFile();
					mfh.enableBinLog(logBinValue); // 修改logbin的值
					SSHcommonUtil.backupFile(session, mycnfFile); // 先备份配置文件， my.cnf -> my.cnf.1

					ScpUtil.to(session, mycnfFile, mfh.toByteArray());
					mysqlUtil.restartMysql(session); // 重启Mysql
					lbs = mysqlUtil.getLogbinState(session, box); // 获取最新的logbin状态。
					if (!lbs.isEnabled()) {
						return FacadeResult.unexpectedResult("mysql.enablelogbin.failed");
					}
					box.getMysqlInstance().setLogBinSetting(lbs);
					box.getMysqlInstance().setReadyForBackup(true);
					boxService.writeDescription(box); // 保存
					return FacadeResult.doneExpectedResult(box, CommonActionResult.DONE);
				}
			}
		} catch (JSchException | IOException | RunRemoteCommandException | ScpException | MysqlAccessDeniedException | MysqlNotStartedException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}
	}

	public FacadeResult<?> updateMysqlDescription(Box box) {
		try {
			boxService.writeDescription(box);
		} catch (IOException e) {
			ExceptionUtil.logErrorException(logger, e);
			FacadeResult.unexpectedResult(e);
		}
		return FacadeResult.doneExpectedResult(box, CommonActionResult.DONE);
	}

	public FacadeResult<?> updateMysqlDescription(Box box, String username, String password, int port,
			String flushLogCron) {
		MysqlInstance mi = box.getMysqlInstance();

		mi.setUsername(username);
		mi.setPassword(password);
		mi.setPort(port);
		if (!flushLogCron.equals(mi.getFlushLogCron())) {
			mi.setFlushLogCron(flushLogCron);
			CronExpressionChangeEvent cece = new CronExpressionChangeEvent(this, BoxUtil.getBorgPruneJobKey(box),
					BoxUtil.getBorgPruneTriggerKey(box), flushLogCron);
			applicationEventPublisher.publishEvent(cece);
		}
		
		box.setMysqlInstance(mi);
		return updateMysqlDescription(box);
	}

	public FacadeResult<String> getMyCnfFile(Session session, Box box) {
		try {
			return FacadeResult.doneExpectedResult(mysqlUtil.getEffectiveMyCnf(session, box), CommonActionResult.DONE);
		} catch (RunRemoteCommandException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}
	}

}
