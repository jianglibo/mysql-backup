package com.go2wheel.mysqlbackup.commands;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.go2wheel.mysqlbackup.SettingsInDb;
import com.go2wheel.mysqlbackup.aop.Exclusive;
import com.go2wheel.mysqlbackup.aop.MeasureTimeCost;
import com.go2wheel.mysqlbackup.exception.ExceptionWrapper;
import com.go2wheel.mysqlbackup.exception.MysqlAccessDeniedException;
import com.go2wheel.mysqlbackup.exception.MysqlNotStartedException;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.exception.ScpException;
import com.go2wheel.mysqlbackup.exception.UnExpectedInputException;
import com.go2wheel.mysqlbackup.expect.MysqlDumpExpect;
import com.go2wheel.mysqlbackup.expect.MysqlFlushLogExpect;
import com.go2wheel.mysqlbackup.model.MysqlDump;
import com.go2wheel.mysqlbackup.model.MysqlInstance;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.MysqlDumpDbService;
import com.go2wheel.mysqlbackup.service.MysqlInstanceDbService;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.util.FileUtil;
import com.go2wheel.mysqlbackup.util.MysqlUtil;
import com.go2wheel.mysqlbackup.util.PathUtil;
import com.go2wheel.mysqlbackup.util.RemotePathUtil;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.util.ScpUtil;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.util.TaskLocks;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.value.FacadeResult.CommonActionResult;
import com.go2wheel.mysqlbackup.value.LinuxLsl;
import com.go2wheel.mysqlbackup.value.LogBinSetting;
import com.go2wheel.mysqlbackup.value.MycnfFileHolder;
import com.go2wheel.mysqlbackup.yml.YamlInstance;
import com.google.common.collect.Lists;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@Service
public class MysqlService {

	public static final String ALREADY_DUMP = "mysql.dump.already";

	private Logger logger = LoggerFactory.getLogger(getClass());

	private MysqlUtil mysqlUtil;
	
	@Autowired
	private SshSessionFactory sshSessionFactory;
	
	@Autowired
	private SettingsInDb settingsInDb;
	
	@Autowired
	private MysqlDumpDbService mysqlDumpDbService;

	@Autowired
	private MysqlInstanceDbService mysqlInstanceDbService;

	@Autowired
	public void setMysqlUtil(MysqlUtil mysqlUtil) {
		this.mysqlUtil = mysqlUtil;
	}

	@Exclusive(TaskLocks.TASK_MYSQL)
	@MeasureTimeCost
	public FacadeResult<LinuxLsl> mysqlDump(Session session, Server server) throws JSchException {
		return mysqlDump(session, server, false);
	}

	private Path getDumpFile(Path dumpDir) {
		return dumpDir.resolve(Paths.get(MysqlUtil.DUMP_FILE_NAME).getFileName());
	}

	public boolean isMysqlNotReadyForBackup(Server server) {
		 return server == null || server.getMysqlInstance() == null || server.getMysqlInstance().getLogBinSetting() == null || server.getMysqlInstance().getLogBinSetting().isEmpty();
	}
	
	
	public CompletableFuture<FacadeResult<LinuxLsl>> mysqlDumpAsync(Server server, boolean force) {
		return CompletableFuture.supplyAsync(() -> {
			FacadeResult<Session> frSession;
			try {
				frSession = sshSessionFactory.getConnectedSession(server);
			} catch (JSchException e) {
				throw new ExceptionWrapper(e);
			}
			return frSession.getResult();
		}).thenApplyAsync(session -> {
			try {
				return this.mysqlDump(session, server, force);
			} catch (JSchException e1) {
				throw new ExceptionWrapper(e1);
			} finally {
				if (session != null && session.isConnected()) {
					session.disconnect();
				}
			}
		}).exceptionally(e -> {
			return FacadeResult.unexpectedResult(((ExceptionWrapper)e).getException());
		});
	}
	

	@Exclusive(TaskLocks.TASK_MYSQL)
	@MeasureTimeCost
	public FacadeResult<LinuxLsl> mysqlDump(Session session, Server server, boolean force) throws JSchException {
		try {
			Path dumpDir = settingsInDb.getDumpDir(server);
			Path localDumpFile = getDumpFile(dumpDir);
			if (Files.exists(localDumpFile) && !force) {
				return FacadeResult.doneExpectedResultPreviousDone(ALREADY_DUMP);
			}
			if (force) {
				// origin dump folder no existing any more.
				FileUtil.backup(dumpDir, 2, 3, true);
				FileUtil.deleteFolder(dumpDir, true);
			}
			List<String> r = new MysqlDumpExpect(session, server).start();
			if (r.size() == 2) {
				LinuxLsl llsl = LinuxLsl.matchAndReturnLinuxLsl(r.get(0)).get();
				llsl.setMd5(r.get(1));
				SSHcommonUtil.downloadWithTmpDownloadingFile(session, llsl.getFilename(), localDumpFile); // cause new dump to create.
				return FacadeResult.doneExpectedResult(llsl, CommonActionResult.DONE);
			} else {
				return FacadeResult.unexpectedResult(r.get(0));
			}
		} catch (IOException | RunRemoteCommandException | ScpException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}
		
	}
	
	public FacadeResult<LinuxLsl> saveDumpResult(Server server, FacadeResult<LinuxLsl> fr) {
		MysqlDump md = new MysqlDump();
		md.setCreatedAt(new Date());
		long tc = fr.getEndTime() - fr.getStartTime();
		md.setTimeCost(tc);
		if (fr.isExpected()) {
			if (fr.getResult() != null) {
				md.setFileSize(fr.getResult().getSize());
			} else if (MysqlService.ALREADY_DUMP.equals(fr.getMessage())) {
			} else {
			}
		} else {
		}
		md.setServerId(server.getId());
		mysqlDumpDbService.save(md);
		return fr;
	}

	@Exclusive(TaskLocks.TASK_MYSQL)
	@MeasureTimeCost
	public FacadeResult<Path> mysqlFlushLogsAndReturnIndexFile(Session session, Server server) throws JSchException, IOException {
		if (server.getMysqlInstance() == null || !server.getMysqlInstance().isReadyForBackup()) {
			throw new UnExpectedInputException(null, "mysql.unready", "");
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
	/**
	 * 执行flush之后，将/var/lib/mysql/hm-log-bin.index下载下来，index文件里面没有的文件下载下来。
	 * @param session
	 * @param server
	 * @return 本地index文件的路径。
	 * @throws JSchException 
	 * @throws IOException 
	 */
	public FacadeResult<Path> downloadBinLog(Session session, Server server) throws JSchException, IOException {
		try {
			LogBinSetting lbs = server.getMysqlInstance().getLogBinSetting();
			String remoteIndexFile = lbs.getLogBinIndex();
			String basenameOnlyName = lbs.getLogBinBasenameOnlyName();

			String binLogIndexOnlyName = lbs.getLogBinIndexNameOnly();

			Path localDir = settingsInDb.getDumpDir(server);
			Path localIndexFile = localDir.resolve(binLogIndexOnlyName);

			if (Files.exists(localIndexFile)) {
				PathUtil.archiveLocalFile(localIndexFile, 6);
			}
			
			localIndexFile = SSHcommonUtil.downloadWithTmpDownloadingFile(session, remoteIndexFile, localIndexFile);

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
				String rfile = RemotePathUtil.getLogBinFile(server, fn);
				Path lfile =  localDir.resolve(fn);
				SSHcommonUtil.downloadWithTmpDownloadingFile(session, rfile, lfile);

			}
			return FacadeResult.doneExpectedResult(localIndexFile, CommonActionResult.DONE);
		} catch (RunRemoteCommandException | ScpException e) {
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
			mi = mysqlInstanceDbService.save(mi);
			server.setMysqlInstance(mi);
			return FacadeResult.doneExpectedResult(server, CommonActionResult.DONE);
		} catch (JSchException | IOException | RunRemoteCommandException | ScpException | MysqlAccessDeniedException | MysqlNotStartedException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}
	}

	public FacadeResult<?> enableLogbin(Session session, Server server) {
		return enableLogbin(session, server, MycnfFileHolder.DEFAULT_LOG_BIN_BASE_NAME);
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
			mfh.setVariables(lbs.getMap());
			
			Path mysqlSettingDir = settingsInDb.getLocalMysqlDir(server);
			Files.write(mysqlSettingDir.resolve("mycnf.yml"), YamlInstance.INSTANCE.yaml.dumpAsMap(mfh).getBytes());
			
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
			mi = mysqlInstanceDbService.save(mi);
			server.setMysqlInstance(mi);
			return FacadeResult.doneExpectedResult(server, CommonActionResult.DONE);
		} catch (JSchException | IOException | RunRemoteCommandException | ScpException | MysqlNotStartedException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}
	}

	public FacadeResult<String> getMyCnfFile(Session session, Server server) {
		try {
			return FacadeResult.doneExpectedResult(mysqlUtil.getEffectiveMyCnf(session, server), CommonActionResult.DONE);
		} catch (RunRemoteCommandException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}
	}

	public FacadeResult<?> getLogbinState(Session session, Server server) throws JSchException, IOException, MysqlAccessDeniedException, MysqlNotStartedException {
	    return FacadeResult.doneExpectedResultDone(mysqlUtil.getLogbinState(session, server).toLines());
	}

	public FacadeResult<?> getMyCnf(Session session, Server server) throws RunRemoteCommandException, IOException, JSchException, ScpException {
		MycnfFileHolder mfh = mysqlUtil.getMyCnfFile(session, server); // 找到起作用的my.cnf配置文件。
		return FacadeResult.doneExpectedResultDone(mfh.getMyCnfFile());

	}

	public List<MysqlDumpFolderWrapper> listLocalDumps(Server server) throws IOException {
		Path lrp = settingsInDb.getDumpDir(server);
		List<Path> pathes = Lists.newArrayList();
		pathes = Files.list(lrp.getParent()).collect(Collectors.toList());
		Collections.sort(pathes, (o1, o2) -> {
			try {
				BasicFileAttributes attr1 = Files.readAttributes(o1, BasicFileAttributes.class);
				BasicFileAttributes attr2 = Files.readAttributes(o2, BasicFileAttributes.class);
				return attr1.lastAccessTime().toInstant().compareTo(attr2.lastAccessTime().toInstant());
			} catch (IOException e) {
				return 0;
			}
		});
		return pathes.stream().map(MysqlDumpFolderWrapper::new).collect(Collectors.toList());
	}
	
	
	public static class MysqlDumpFolderWrapper {
		private final Path dump;

		public MysqlDumpFolderWrapper(Path repo) {
			this.dump = repo;
		}

		public Path getRepo() {
			return dump;
		}
		
		public String getDumpFolderName() {
			return dump.getFileName().toString();
		}

		public Date getLastAccessTime() throws IOException {
			BasicFileAttributes bfa = Files.readAttributes(dump, BasicFileAttributes.class);
			return new Date(bfa.lastAccessTime().toMillis());
		}

		public String getSize() throws IOException {
			long size = Files.walk(dump).filter(Files::isRegularFile).mapToLong(value -> {
				try {
					return Files.size(value);
				} catch (IOException e) {
					return 0L;
				}
			}).sum();
			return StringUtil.formatSize(size, 2);
		}
		
	}

}
