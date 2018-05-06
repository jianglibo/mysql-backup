package com.go2wheel.mysqlbackup.commands;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.MyAppSettings;
import com.go2wheel.mysqlbackup.exception.AtomicWriteFileException;
import com.go2wheel.mysqlbackup.exception.CreateDirectoryException;
import com.go2wheel.mysqlbackup.exception.EnableLogBinFailedException;
import com.go2wheel.mysqlbackup.exception.LocalBackupFileException;
import com.go2wheel.mysqlbackup.exception.LocalFileMoveException;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.exception.ScpToException;
import com.go2wheel.mysqlbackup.expect.MysqlDumpExpect;
import com.go2wheel.mysqlbackup.expect.MysqlFlushLogExpect;
import com.go2wheel.mysqlbackup.util.FileUtil;
import com.go2wheel.mysqlbackup.util.MysqlUtil;
import com.go2wheel.mysqlbackup.util.PathUtil;
import com.go2wheel.mysqlbackup.util.RemotePathUtil;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.util.ScpUtil;
import com.go2wheel.mysqlbackup.util.TaskLocks;
import com.go2wheel.mysqlbackup.util.StringUtil.LinuxFileInfo;
import com.go2wheel.mysqlbackup.value.Box;
import com.go2wheel.mysqlbackup.value.LogBinSetting;
import com.go2wheel.mysqlbackup.value.MycnfFileHolder;
import com.go2wheel.mysqlbackup.value.MysqlDumpResult;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@Component
public class MysqlTaskFacade {

	private MysqlUtil mysqlUtil;
	
	private MyAppSettings appSettings;

	@Autowired
	public void setMysqlUtil(MysqlUtil mysqlUtil) {
		this.mysqlUtil = mysqlUtil;
	}

	@Autowired
	public void setAppSettings(MyAppSettings appSettings) {
		this.appSettings = appSettings;
	}

	
	public MysqlDumpResult mysqlDump(Session session, Box box) {
		return mysqlDump(session, box, false);
	}

	public MysqlDumpResult mysqlDump(Session session, Box box, boolean force) {
		Lock lock = TaskLocks.getBoxLock(box.getHost(), TaskLocks.TASK_MYSQL);
		if (lock.tryLock()) {
			try {
				Path localDumpFile = appSettings.getDumpDir(box)
						.resolve(Paths.get(MysqlUtil.DUMP_FILE_NAME).getFileName());
				if (Files.exists(localDumpFile) && !force) {
					return MysqlDumpResult.failedResult("mysqldump文件已经存在，再次执行意味着之前的logbin文件可能失去效用。");
				}
				
				if (force) {
					Path dumpDir = appSettings.getDumpDir(box);
					Path logbinDir = appSettings.getLogBinDir(box);
					
					FileUtil.createNewBackupAndRemoveOrigin(3, dumpDir, logbinDir);
					try {
						Files.createDirectories(dumpDir);
						Files.createDirectories(logbinDir);
					} catch (IOException e) {
						return MysqlDumpResult.failedResult("重建dump和logbin目录失败。");
					}
				}
				Optional<LinuxFileInfo> ll;
				ll = new MysqlDumpExpect(session, box).start();
				if (ll.isPresent()) {
					mysqlUtil.downloadDumped(session, box, ll.get());
					return MysqlDumpResult.successResult(ll.get());
				} else {
					return MysqlDumpResult.failedResult("unknown");
				}
			} catch (LocalBackupFileException | LocalFileMoveException e) {
				return MysqlDumpResult.failedResult("备份目录失败。");
			} finally {
				lock.unlock();
			}
		} else {
			return MysqlDumpResult.failedResult("任务进行中，请稍后再试。");
		}
	}

	public String mysqlFlushLogs(Session session, Box box) {
		Lock lock = TaskLocks.getBoxLock(box.getHost(), TaskLocks.TASK_MYSQL);
		if (lock.tryLock()) {
			try {
				MysqlFlushLogExpect mfle = new MysqlFlushLogExpect(session, box);
				if (mfle.start()) {
					return "flush成功";
				} else {
					return "flush失败";
				}
			} finally {
				lock.unlock();
			}
		} else {
			return "任务进行中，请稍后再试。";
		}
	}

	public String downloadBinLog(Session session, Box box) throws RunRemoteCommandException, CreateDirectoryException {
		String remoteIndexFile = box.getMysqlInstance().getLogBinSetting().getLogBinIndex();
		String basenameOnlyName = box.getMysqlInstance().getLogBinSetting().getLogBinBasenameOnlyName();

		String binLogIndexOnlyName = box.getMysqlInstance().getLogBinSetting().getLogBinIndexNameOnly();

		Path localDir = appSettings.getLogBinDir(box);
		Path localIndexFile = localDir.resolve(binLogIndexOnlyName);

		if (Files.exists(localIndexFile)) {
			PathUtil.archiveLocalFile(localIndexFile, 6);
		}
		SSHcommonUtil.downloadWithTmpDownloadingFile(session, remoteIndexFile, localIndexFile);

		try {
			List<String> localBinLogFiles = Files.list(localDir).map(p -> p.getFileName().toString())
					.collect(Collectors.toList());

			List<String> unLocalExists = Files.lines(localIndexFile).filter(l -> l.indexOf(basenameOnlyName) != -1)
					.map(l -> l.trim()).map(l -> Paths.get(l).getFileName().toString())
					.filter(l -> !localBinLogFiles.contains(l)).collect(Collectors.toList());

			unLocalExists.forEach(f -> {
				try {
					SSHcommonUtil.downloadWithTmpDownloadingFile(session, RemotePathUtil.getLogBinFile(box, f),
							localDir.resolve(f));
				} catch (RunRemoteCommandException e) {
					e.printStackTrace();
				} catch (CreateDirectoryException e) {
					e.printStackTrace();
				}
			});

		} catch (IOException e) {
			return e.getMessage();
		}
		return "success";
	}

	public String mysqlEnableLogbin(Session session, Box box, String logBinValue) throws IOException, CreateDirectoryException, AtomicWriteFileException, JSchException, RunRemoteCommandException {
		LogBinSetting lbs = box.getMysqlInstance().getLogBinSetting();
		if (lbs != null && lbs.isEnabled()) {
			return "本地服务器描述显示LogBin已经启用。";
		} else {
			lbs = mysqlUtil.getLogbinState(session, box);
			if (lbs.isEnabled()) {
				box.getMysqlInstance().setLogBinSetting(lbs);
				mysqlUtil.writeDescription(box);
				return "本地服务器描述显示LogBin未启用，但远程显示已经启动，修改本地描述。";
			} else {
				MycnfFileHolder mfh = mysqlUtil.getMyCnfFile(session, box); // 找到起作用的my.cnf配置文件。
				String mycnfFile = box.getMysqlInstance().getMycnfFile();
				mfh.enableBinLog(logBinValue); // 修改logbin的值
				SSHcommonUtil.backupFile(session, mycnfFile); // 先备份配置文件， my.cnf -> my.cnf.1
				try {
					ScpUtil.to(session, mycnfFile, mfh.toByteArray());
				} catch (ScpToException e) {
					e.printStackTrace();
				} // 覆盖写入 my.cnf
				mysqlUtil.restartMysql(session); // 重启Mysql
				lbs = mysqlUtil.getLogbinState(session, box); // 获取最新的logbin状态。
				if (!lbs.isEnabled()) {
					throw new EnableLogBinFailedException(box.getHost());
				}
				box.getMysqlInstance().setLogBinSetting(lbs);
				mysqlUtil.writeDescription(box); // 保存
			}
		}
		return lbs.toString();
	}

}
