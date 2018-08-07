package com.go2wheel.mysqlbackup.commands;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.go2wheel.mysqlbackup.SettingsInDb;
import com.go2wheel.mysqlbackup.aop.Exclusive;
import com.go2wheel.mysqlbackup.aop.MeasureTimeCost;
import com.go2wheel.mysqlbackup.exception.AppNotStartedException;
import com.go2wheel.mysqlbackup.exception.ExceptionWrapper;
import com.go2wheel.mysqlbackup.exception.MysqlAccessDeniedException;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.exception.ScpException;
import com.go2wheel.mysqlbackup.exception.UnExpectedContentException;
import com.go2wheel.mysqlbackup.exception.UnExpectedInputException;
import com.go2wheel.mysqlbackup.expect.MysqlDumpExpect;
import com.go2wheel.mysqlbackup.expect.MysqlFlushLogExpect;
import com.go2wheel.mysqlbackup.expect.MysqlImportExpect;
import com.go2wheel.mysqlbackup.model.MysqlDump;
import com.go2wheel.mysqlbackup.model.MysqlInstance;
import com.go2wheel.mysqlbackup.model.PlayBack;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.StorageState;
import com.go2wheel.mysqlbackup.service.MysqlDumpDbService;
import com.go2wheel.mysqlbackup.service.MysqlInstanceDbService;
import com.go2wheel.mysqlbackup.service.StorageStateService;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.util.MysqlUtil;
import com.go2wheel.mysqlbackup.util.RemotePathUtil;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.util.ScpUtil;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.go2wheel.mysqlbackup.util.TaskLocks;
import com.go2wheel.mysqlbackup.value.AsyncTaskValue;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.value.FacadeResult.CommonActionResult;
import com.go2wheel.mysqlbackup.value.LinuxLsl;
import com.go2wheel.mysqlbackup.value.MycnfFileHolder;
import com.go2wheel.mysqlbackup.value.MysqlDumpFolder;
import com.go2wheel.mysqlbackup.value.MysqlVariables;
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
	private StorageStateService sss;

	@Autowired
	public void setMysqlUtil(MysqlUtil mysqlUtil) {
		this.mysqlUtil = mysqlUtil;
	}

	@Exclusive(TaskLocks.TASK_MYSQL)
	@MeasureTimeCost
	public FacadeResult<LinuxLsl> mysqlDump(Session session, Server server) throws JSchException, IOException, NoSuchAlgorithmException, UnExpectedContentException {
		return mysqlDump(session, server, false);
	}

	private Path getDumpFile(Path dumpDir) {
		return dumpDir.resolve(Paths.get(MysqlUtil.DUMP_FILE_NAME).getFileName());
	}

	public boolean isMysqlNotReadyForBackup(Server server) {
		 return server == null || server.getMysqlInstance() == null || server.getMysqlInstance().getLogBinSetting() == null || server.getMysqlInstance().getLogBinSetting().isEmpty();
	}
	
	
	public CompletableFuture<AsyncTaskValue> mysqlDumpAsync(Server server, String taskDescription, boolean force) {
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
				FacadeResult<LinuxLsl> fr = this.mysqlDump(session, server, force);
				return new AsyncTaskValue(fr).withDescription(taskDescription);
			} catch (JSchException | IOException | NoSuchAlgorithmException | UnExpectedContentException e1) {
				throw new ExceptionWrapper(e1);
			} finally {
				if (session != null && session.isConnected()) {
					session.disconnect();
				}
			}
		}).exceptionally(e -> {
			return new AsyncTaskValue(FacadeResult.unexpectedResult(((ExceptionWrapper)e).getException())).withDescription(taskDescription);
		});
	}
	

	/**
	 * You can dump database at any time, each dump create a new folder.
	 * @param session
	 * @param server
	 * @param force
	 * @return
	 * @throws JSchException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws UnExpectedContentException 
	 * @throws AppNotStartedException 
	 */
	@Exclusive(TaskLocks.TASK_MYSQL)
	@MeasureTimeCost
	public FacadeResult<LinuxLsl> mysqlDump(Session session, Server server, boolean force) throws JSchException, IOException, NoSuchAlgorithmException, UnExpectedContentException {
		
		try {
			Path dumpDir = settingsInDb.getNextDumpDir(server);
			Path localDumpFile = getDumpFile(dumpDir);
			
			List<String> r = new MysqlDumpExpect(session, server).start();
			if (r.size() == 2) {
				LinuxLsl llsl = LinuxLsl.matchAndReturnLinuxLsl(r.get(0)).get();
				llsl.setMd5(r.get(1));
				SSHcommonUtil.downloadWithTmpDownloadingFile(session, llsl.getFilename(), localDumpFile); // cause new dump to create.
				backupMysqlSettings(session, server);
				return FacadeResult.doneExpectedResult(llsl, CommonActionResult.DONE);
			} else {
				return FacadeResult.unexpectedResult(r.get(0));
			}
		} catch (RunRemoteCommandException | ScpException | AppNotStartedException e) {
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
	public FacadeResult<Path> mysqlFlushLogsAndReturnIndexFile(Session session, Server server) throws JSchException, IOException, NoSuchAlgorithmException, UnExpectedInputException, UnExpectedContentException {
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
	 * @throws NoSuchAlgorithmException 
	 */
	public FacadeResult<Path> downloadBinLog(Session session, Server server) throws JSchException, IOException, NoSuchAlgorithmException {
		try {
			MysqlVariables lbs = server.getMysqlInstance().getLogBinSetting();
			String remoteIndexFile = lbs.getLogBinIndex();
			String basenameOnlyName = lbs.getLogBinBasenameOnlyName();

			String binLogIndexOnlyName = lbs.getLogBinIndexNameOnly();

			Path localDir = settingsInDb.getCurrentDumpDir(server);
			Path localIndexFile = localDir.resolve(binLogIndexOnlyName);
			
			localIndexFile = SSHcommonUtil.downloadWithTmpDownloadingFile(session, remoteIndexFile, localIndexFile, 7);

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
	
	public FacadeResult<?> disableLogbin(Session session, Server server) throws UnExpectedContentException {
		try {

			MysqlInstance mi = server.getMysqlInstance();
			MysqlVariables lbs = mi.getLogBinSetting();
			try {
				lbs = mysqlUtil.getLogbinState(session, server);
			} catch (AppNotStartedException e) {
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
		} catch (JSchException | IOException | RunRemoteCommandException | ScpException | MysqlAccessDeniedException | AppNotStartedException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}
	}
	
	public MycnfFileHolder requestMysqlSettings(Server server) throws JSchException, IOException, AppNotStartedException, RunRemoteCommandException, UnExpectedContentException, ScpException {
		Session session = null;
		try {
			session = sshSessionFactory.getConnectedSession(server).getResult();
			return requestMysqlSettings(session, server);
		} finally {
			if( session != null) {
				session.disconnect();
			}
		}
	}
	
	public MycnfFileHolder requestMysqlSettings(Session session, Server server) throws JSchException, IOException, AppNotStartedException, RunRemoteCommandException, UnExpectedContentException, ScpException {
		MysqlVariables lbs = mysqlUtil.getLogbinState(session, server);
		MycnfFileHolder mfh = mysqlUtil.getMyCnfFile(session, server); // 找到起作用的my.cnf配置文件。
		mfh.setVariables(lbs.getMap());
		return mfh;
	}
	
	public MycnfFileHolder backupMysqlSettings(Session session, Server server) throws JSchException, IOException, AppNotStartedException, RunRemoteCommandException, UnExpectedContentException, ScpException {
		Path mysqlSettingDir = settingsInDb.getLocalMysqlDir(server);
		MycnfFileHolder mfh = requestMysqlSettings(session, server);
		Files.write(mysqlSettingDir.resolve("mycnf.yml"), YamlInstance.INSTANCE.yaml.dumpAsMap(mfh).getBytes());
		return mfh;
	}
	
	public MycnfFileHolder getMysqlSettingsFromDisk(Server server) throws IOException {
		Path mysqlSettingDir = settingsInDb.getLocalMysqlDir(server);
		try (InputStream is = Files.newInputStream(mysqlSettingDir.resolve("mycnf.yml"))) {
			return YamlInstance.INSTANCE.yaml.loadAs(is, MycnfFileHolder.class);
		}
	}
	
	public void overWriteMycnf(Session session, Server server, MycnfFileHolder mfh) throws ScpException {
		ScpUtil.to(session, mfh.getMyCnfFile(), mfh.toByteArray());
	}

	public FacadeResult<?> enableLogbin(Session session, Server server) throws UnExpectedContentException {
		return enableLogbin(session, server, server.getMysqlInstance().getLogBinSetting().getLogBinBasename());
	}
	
	public FacadeResult<?> enableLogbin(Session session, Server server, String logBinValue) throws UnExpectedContentException {
		try {
			MysqlInstance mi = server.getMysqlInstance();
			
			MycnfFileHolder mfh = backupMysqlSettings(session, server);
			String mycnfFile = mfh.getMyCnfFile();
			MysqlVariables lbs = mfh.getMysqlVariables();
			
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
		} catch (JSchException | IOException | RunRemoteCommandException | ScpException | AppNotStartedException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}
	}

	public FacadeResult<String> getMyCnfFile(Session session, Server server) throws UnExpectedContentException {
		try {
			return FacadeResult.doneExpectedResult(mysqlUtil.getEffectiveMyCnf(session, server), CommonActionResult.DONE);
		} catch (RunRemoteCommandException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}
	}

	public FacadeResult<?> getLogbinState(Session session, Server server) throws JSchException, IOException, MysqlAccessDeniedException, AppNotStartedException {
	    return FacadeResult.doneExpectedResultDone(mysqlUtil.getLogbinState(session, server).toLines());
	}

	public FacadeResult<?> getMyCnf(Session session, Server server) throws RunRemoteCommandException, IOException, JSchException, ScpException, UnExpectedContentException {
		MycnfFileHolder mfh = mysqlUtil.getMyCnfFile(session, server); // 找到起作用的my.cnf配置文件。
		return FacadeResult.doneExpectedResultDone(mfh.getMyCnfFile());

	}
	
	public CompletableFuture<AsyncTaskValue> restoreAsync(PlayBack playback, Server sourceServer, Server targetServer, String dumpFolder) throws IOException, JSchException, RunRemoteCommandException, UnExpectedContentException, AppNotStartedException, ScpException {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return new AsyncTaskValue(restore(playback, sourceServer, targetServer, dumpFolder));
			} catch (RunRemoteCommandException | UnExpectedContentException | IOException | JSchException
					| AppNotStartedException | ScpException e1) {
				throw new ExceptionWrapper(e1);
			}
		}).exceptionally(e -> {
			return new AsyncTaskValue(false);
		});
	}
	

	public Boolean restore(PlayBack playback, Server sourceServer, Server targetServer, String dumpFolder) throws IOException, JSchException, RunRemoteCommandException, UnExpectedContentException, AppNotStartedException, ScpException {
		Session targetSession = null;
		try {
			targetSession = sshSessionFactory.getConnectedSession(targetServer).getResult();
			String remoteFolder = uploadDumpFolder(sourceServer, targetServer, targetSession, dumpFolder);
			String dumpfn = RemotePathUtil.getFileName(sourceServer.getMysqlInstance().getDumpFileName());
			dumpfn = RemotePathUtil.join(remoteFolder, dumpfn);
			List<String> lines = importDumped(targetSession, targetServer, sourceServer.getMysqlInstance(), targetServer.getMysqlInstance(), remoteFolder);
			if (lines.size() == 1 && lines.get(0).isEmpty()) {
				return true;
			} else {
				return false;
			}
		} finally {
			if( targetSession != null) {
				targetSession.disconnect();
			}
		}
	}
	
	public String uploadDumpFolder(Server sourceServer, Server targetServer,Session targetSession, Path dumpFolder) throws IOException {
		List<StorageState> ss = sss.getStorageState(targetServer, targetSession, false);
		Collections.sort(ss, StorageState.AVAILABLE_DESC);
		
		String remoteMaxRoot = ss.get(0).getRoot();
		String remoteDumpFolder = RemotePathUtil.join(remoteMaxRoot, dumpFolder.getFileName().toString());
		SSHcommonUtil.deleteRemoteFolder(targetSession, remoteDumpFolder);
		SSHcommonUtil.copyFolder(targetSession, dumpFolder, remoteDumpFolder);
		return remoteDumpFolder;
	}
	
	public String uploadDumpFolder(Server sourceServer, Server targetServer,Session targetSession, String dumpFolder) throws IOException {
		Path dumpFolderToUpload = settingsInDb.getDumpsDir(sourceServer).resolve(dumpFolder);
		return uploadDumpFolder(sourceServer, targetServer, targetSession, dumpFolderToUpload);
	}

	public List<MysqlDumpFolder> listDumpFolders(Server sourceServer) throws IOException {
		Path dumps = settingsInDb.getDumpsDir(sourceServer);
		List<MysqlDumpFolder> dumpFolders = Files.list(dumps).map(MysqlDumpFolder::newInstance).filter(Objects::nonNull).collect(Collectors.toList());
		Collections.sort(dumpFolders);
		return dumpFolders;
	}
	
	public CompletableFuture<List<String>> importDumpedAsync(Server targetServer, MysqlInstance sourceMysqlInstance,
			MysqlInstance targetMysqlInstance, String remoteFolder) throws UnExpectedContentException {
		return CompletableFuture.supplyAsync(() -> {
			FacadeResult<Session> frSession;
			try {
				frSession = sshSessionFactory.getConnectedSession(targetServer);
			} catch (JSchException e) {
				throw new ExceptionWrapper(e);
			}
			return frSession.getResult();
		}).thenApplyAsync(targetSession -> {
			try {
				return this.importDumped(targetSession, targetServer, sourceMysqlInstance, targetMysqlInstance, remoteFolder);
			} catch (UnExpectedContentException e1) {
				throw new ExceptionWrapper(e1);
			} finally {
				if (targetSession != null && targetSession.isConnected()) {
					targetSession.disconnect();
				}
			}
		}).exceptionally(e -> {
			return Lists.newArrayList();
		});
	}

	public List<String> importDumped(Session targetSession, Server targetServer, MysqlInstance sourceMysqlInstance,
			MysqlInstance targetMysqlInstance, String remoteFolder) throws UnExpectedContentException {
		String dumpfn = RemotePathUtil.getFileName(sourceMysqlInstance.getDumpFileName());
		dumpfn = RemotePathUtil.join(remoteFolder, dumpfn);
		
		MysqlImportExpect mie = new MysqlImportExpect(targetSession, targetServer, dumpfn);
		return mie.start();
	}
}
