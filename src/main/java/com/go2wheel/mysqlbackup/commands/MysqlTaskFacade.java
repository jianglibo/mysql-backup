package com.go2wheel.mysqlbackup.commands;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.exception.EnableLogBinFailedException;
import com.go2wheel.mysqlbackup.expect.MysqlDumpExpect;
import com.go2wheel.mysqlbackup.expect.MysqlFlushLogExpect;
import com.go2wheel.mysqlbackup.util.MysqlUtil;
import com.go2wheel.mysqlbackup.util.PathUtil;
import com.go2wheel.mysqlbackup.util.RemotePathUtil;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.util.ScpUtil;
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

	@Autowired
	public void setMysqlUtil(MysqlUtil mysqlUtil) {
		this.mysqlUtil = mysqlUtil;
	}

	public MysqlDumpResult mysqlDump(Session session, Box box) {
		Path localDumpFile = mysqlUtil.getDumpDir(box).resolve(Paths.get(MysqlUtil.DUMP_FILE_NAME).getFileName());
		if (Files.exists(localDumpFile)) {
			return MysqlDumpResult.failedResult("mysqldump文件已经存在，再次执行意味着之前的logbin文件可能失去效用。");
		}
		Optional<LinuxFileInfo> ll;
		ll = new MysqlDumpExpect(session, box).start();
		if (ll.isPresent()) {
			mysqlUtil.downloadDumped(session, box, ll.get());
			return MysqlDumpResult.successResult(ll.get());
		} else {
			return MysqlDumpResult.failedResult("unknown");
		}
	}
	
	public void mysqlFlushLogs(Session session, Box box) {
		MysqlFlushLogExpect mfle = new MysqlFlushLogExpect(session, box);
		boolean success = mfle.start();
	}

	public String downloadBinLog(Session session, Box box) {
		String remoteIndexFile = box.getMysqlInstance().getLogBinSetting().getLogBinIndex();
		String basenameOnlyName = box.getMysqlInstance().getLogBinSetting().getLogBinBasenameOnlyName();

		String binLogIndexOnlyName = box.getMysqlInstance().getLogBinSetting().getLogBinIndexNameOnly();

		Path localDir = mysqlUtil.getLogBinDir(box);
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
				SSHcommonUtil.downloadWithTmpDownloadingFile(session, RemotePathUtil.getLogBinFile(box, f),
						localDir.resolve(f));
			});

		} catch (IOException e) {
			return e.getMessage();
		}
		return "success";
	}

	public String mysqlEnableLogbin(Session session, Box box, String logBinValue) throws JSchException, IOException {
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
				ScpUtil.to(session, mycnfFile, mfh.toByteArray()); // 覆盖写入 my.cnf
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
