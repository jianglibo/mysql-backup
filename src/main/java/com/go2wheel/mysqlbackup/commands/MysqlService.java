package com.go2wheel.mysqlbackup.commands;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import com.go2wheel.mysqlbackup.exception.UnExpectedInputException;
import com.go2wheel.mysqlbackup.exception.UnExpectedOutputException;
import com.go2wheel.mysqlbackup.expect.MysqlDumpExpect;
import com.go2wheel.mysqlbackup.expect.MysqlFlushLogExpect;
import com.go2wheel.mysqlbackup.expect.MysqlPasswordReadyExpect;
import com.go2wheel.mysqlbackup.installer.MySqlInstaller;
import com.go2wheel.mysqlbackup.installer.MysqlInstallInfo;
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
import com.go2wheel.mysqlbackup.util.PSUtil;
import com.go2wheel.mysqlbackup.util.PathUtil;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.util.ScpUtil;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.util.TaskLocks;
import com.go2wheel.mysqlbackup.value.AsyncTaskValue;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.value.FacadeResult.CommonActionResult;
import com.go2wheel.mysqlbackup.value.LinuxLsl;
import com.go2wheel.mysqlbackup.value.MycnfFileHolder;
import com.go2wheel.mysqlbackup.value.MysqlDumpFolder;
import com.go2wheel.mysqlbackup.value.MysqlVariables;
import com.go2wheel.mysqlbackup.value.OsTypeWrapper;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.go2wheel.mysqlbackup.value.RemoteFileDescription;
import com.go2wheel.mysqlbackup.value.RemoteFileDescriptionImpl;
import com.go2wheel.mysqlbackup.yml.YamlInstance;
import com.google.common.collect.Lists;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@Service
public class MysqlService {

	public static final String ALREADY_DUMP = "mysql.dump.already";
	
	public static final String DUMP_TASK_KEY = "taskkey.dump";

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
	private MySqlInstaller mySqlInstaller;

	@Autowired
	public void setMysqlUtil(MysqlUtil mysqlUtil) {
		this.mysqlUtil = mysqlUtil;
	}


	public boolean isMysqlNotReadyForBackup(Server server) {
		 return server == null || server.getMysqlInstance() == null || server.getMysqlInstance().getLogBinSetting() == null || server.getMysqlInstance().getLogBinSetting().isEmpty();
	}
	
	
	public CompletableFuture<AsyncTaskValue> mysqlDumpAsync(Server server, String taskDescription, Long id) {
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
				FacadeResult<RemoteFileDescription> fr = this.mysqlDump(session, server);
				return new AsyncTaskValue(id, fr).withDescription(taskDescription);
			} catch (JSchException | IOException | NoSuchAlgorithmException | UnExpectedOutputException | MysqlAccessDeniedException | UnExpectedInputException e1) {
				throw new ExceptionWrapper(e1);
			} finally {
				if (session != null && session.isConnected()) {
					session.disconnect();
				}
			}
		}).exceptionally(e -> {
			ExceptionWrapper e1 = (ExceptionWrapper) e.getCause();
			return new AsyncTaskValue(id, FacadeResult.unexpectedResult(e1.getException())).withDescription(taskDescription);
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
	 * @throws UnExpectedOutputException 
	 * @throws MysqlAccessDeniedException 
	 * @throws UnExpectedInputException 
	 * @throws AppNotStartedException 
	 */
	@Exclusive(TaskLocks.TASK_MYSQL)
	@MeasureTimeCost
	public FacadeResult<RemoteFileDescription> mysqlDump(Session session, Server server) throws JSchException, IOException, NoSuchAlgorithmException, UnExpectedOutputException, MysqlAccessDeniedException, UnExpectedInputException {
		OsTypeWrapper owr = OsTypeWrapper.of(server.getOs());
		
		if (owr.isWin()) {
			try {
				
//				e:\wamp64\bin\mysql\mysql5.7.21\bin\mysqldump --max_allowed_packet=512M -uroot -p --quick --events --all-databases --flush-logs --delete-master-logs --single-transaction > e:\tmp\mysqldump.sql; '---end---'
				
				Path dumpDir = settingsInDb.getNextDumpDir(server);
				//localDump file name is fixed. But remote dump file name varies.
				
				Path localDumpFile = dumpDir.resolve(Paths.get(MysqlUtil.FIXED_DUMP_FILE_NAME).getFileName());
				
				// remote dump file varies.
				String rdump = server.getMysqlInstance().getDumpFileName();
				String rdumpp = PathUtil.getParentWithoutEndingSeperator(rdump);
				RemoteCommandResult rcr = SSHcommonUtil.mkdirsp(server.getOs(), session, rdumpp);
				
				if (rcr.isExitValueNotEqZero()) {
					if (rcr.getAllTrimedNotEmptyLines().stream().anyMatch(line -> line.contains("ObjectNotFound"))) {
						throw new UnExpectedInputException("1000", "mysql.dumpfilename", rcr.getAllTrimedNotEmptyLines().stream().collect(Collectors.joining("\n")));
					}
				}
				
				MysqlInstance mi = server.getMysqlInstance();
				String clientDumpBin = StringUtil.hasAnyNonBlankWord(mi.getClientBin()) ? PathUtil.replaceFileName(mi.getClientBin(), "mysqldump") : "mysqldump";
				String cmd = "%s --max_allowed_packet=512M -u%s -p%s --quick --events --all-databases --flush-logs --delete-master-logs --single-transaction > %s;(Get-Item -Path %s | Select-Object -Property FullName,Length), (Get-FileHash -Path %s -Algorithm MD5) | Format-List";
				cmd = String.format(cmd, clientDumpBin, mi.getUsername(),mi.getPassword(), mi.getDumpFileName(), mi.getDumpFileName(), mi.getDumpFileName());
				rcr = SSHcommonUtil.runRemoteCommand(session, cmd);
				Map<String, String> map = PSUtil.parseFormatList(rcr.getStdOutList()).get(0);
				
//				{Path=E:\tmp\mysqldump.sql, Hash=448168A11F434570FBFFF0A3B660AB2E, Algorithm=MD5}
//				{Path=E:\tmp\mysqldump.sql, Length=1572718, FullName=E:\tmp\mysqldump.sql, Hash=014FC8B59E500A4D97C35684309D6D4F, Algorithm=MD5}
				
				SSHcommonUtil.downloadWithTmpDownloadingFile(server.getOs(), session, map.get("Path"), map.get("Hash"), localDumpFile); // cause new dump to create.
				return FacadeResult.doneExpectedResult(RemoteFileDescriptionImpl.of(map.get("Path"), map.get(("Length"))), CommonActionResult.DONE);
			} catch (RunRemoteCommandException | ScpException e) {
				ExceptionUtil.logErrorException(logger, e);
				return FacadeResult.unexpectedResult(e);
			}			
		} else {
			try {
				Path dumpDir = settingsInDb.getNextDumpDir(server);
				//localDump file name is fixed. But remote dump file name varies.
				
				Path localDumpFile = dumpDir.resolve(Paths.get(MysqlUtil.FIXED_DUMP_FILE_NAME).getFileName());
				
				// remote dump file varies.
				String rdump = server.getMysqlInstance().getDumpFileName();
				String rdumpp = PathUtil.getParentWithoutEndingSeperator(rdump);
				SSHcommonUtil.mkdirsp(server.getOs(), session, rdumpp);
				
				List<String> r = new MysqlDumpExpect(session, server).start();
				if (r.size() == 2) {
					LinuxLsl llsl = LinuxLsl.matchAndReturnLinuxLsl(r.get(0)).get();
					llsl.setMd5(r.get(1));
					SSHcommonUtil.downloadWithTmpDownloadingFile(server.getOs(), session, llsl.getFilename(),llsl.getMd5(), localDumpFile); // cause new dump to create.
					backupMysqlSettingsTolocalDisk(session, server);
					return FacadeResult.doneExpectedResult(llsl, CommonActionResult.DONE);
				} else {
					return FacadeResult.unexpectedResult(r.get(0));
				}
			} catch (RunRemoteCommandException | ScpException | AppNotStartedException e) {
				ExceptionUtil.logErrorException(logger, e);
				return FacadeResult.unexpectedResult(e);
			}
		}
	}

	
	public FacadeResult<RemoteFileDescription> saveDumpResult(Server server, FacadeResult<RemoteFileDescription> fr) {
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
	public FacadeResult<Path> mysqlFlushLogsAndReturnIndexFile(Session session, Server server) throws JSchException, IOException, NoSuchAlgorithmException, UnExpectedInputException, UnExpectedOutputException, MysqlAccessDeniedException {
		if (OsTypeWrapper.of(server.getOs()).isWin()) {
			MysqlInstance mi = server.getMysqlInstance();
			
			String mysqladmin = PathUtil.replaceFileName(mi.getClientBin(), "mysqladmin");
			
			String cmd = "%s -u%s -p%s flush-logs";
			cmd = String.format(cmd, mysqladmin, mi.getUsername(), mi.getPassword());
			RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, cmd);
			return downloadBinLog(session, server);
		} else {
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
		if (OsTypeWrapper.of(server.getOs()).isWin()) {
			try {
				MysqlVariables lbs = server.getMysqlInstance().getLogBinSetting();
				String remoteIndexFile = lbs.getLogBinIndex();
				String basenameOnlyName = PathUtil.getFileName(lbs.getLogBinBasenameOnlyName());

				String binLogIndexOnlyName = PathUtil.getFileName(lbs.getLogBinIndexNameOnly());

				Path localDir = settingsInDb.getCurrentDumpDir(server);
				Path localIndexFile = localDir.resolve(binLogIndexOnlyName);
				
				localIndexFile = PathUtil.getNextAvailableByBaseName(localIndexFile, 7);
				
				String command = String.format("Get-Content -Path %s", remoteIndexFile);
				
				RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, command);
				
				Files.write(localIndexFile, rcr.getAllTrimedNotEmptyLines());
				

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
					String rfile = PathUtil.getLogBinFile(server, fn);
					Path lfile =  localDir.resolve(fn);
					SSHcommonUtil.downloadWithTmpDownloadingFile(server.getOs(), session, rfile,null, lfile);

				}
				return FacadeResult.doneExpectedResult(localIndexFile, CommonActionResult.DONE);
			} catch (RunRemoteCommandException | ScpException e) {
				ExceptionUtil.logErrorException(logger, e);
				return FacadeResult.unexpectedResult(e);
			}
			
		} else {
			try {
				MysqlVariables lbs = server.getMysqlInstance().getLogBinSetting();
				String remoteIndexFile = lbs.getLogBinIndex();
				String basenameOnlyName = lbs.getLogBinBasenameOnlyName();

				String binLogIndexOnlyName = lbs.getLogBinIndexNameOnly();

				Path localDir = settingsInDb.getCurrentDumpDir(server);
				Path localIndexFile = localDir.resolve(binLogIndexOnlyName);
				
				String rmd5 = SSHcommonUtil.getRemoteFileMd5(server.getOs(), session, remoteIndexFile);
				
				localIndexFile = SSHcommonUtil.downloadWithTmpDownloadingFileWithNewVersion(server.getOs(), session, remoteIndexFile, rmd5, localIndexFile, 7);

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
					String rfile = PathUtil.getLogBinFile(server, fn);
					Path lfile =  localDir.resolve(fn);
					SSHcommonUtil.downloadWithTmpDownloadingFile(server.getOs(), session, rfile,null, lfile);

				}
				return FacadeResult.doneExpectedResult(localIndexFile, CommonActionResult.DONE);
			} catch (RunRemoteCommandException | ScpException e) {
				ExceptionUtil.logErrorException(logger, e);
				return FacadeResult.unexpectedResult(e);
			}			
		}
	}
	
	public FacadeResult<?> disableLogbin(Session session, Server server) throws UnExpectedOutputException, MysqlAccessDeniedException, UnExpectedInputException {
		try {

			MysqlInstance mi = server.getMysqlInstance();
			if (mi == null) {
				throw new UnExpectedInputException("1000", "application.notconfiguraeted.mysqlinstance", "No mysqlinstance configuration.");
			}
			MysqlVariables lbs = mi.getLogBinSetting();
			try {
				lbs = mysqlUtil.getLogbinState(session, server);
			} catch (AppNotStartedException e) {
				mysqlUtil.restartMysql(session, server);
				lbs = mysqlUtil.getLogbinState(session, server);
			}
			
			MycnfFileHolder mfh = mysqlUtil.getMyCnfFile(session, server); // 找到起作用的my.cnf配置文件。
			String mycnfFile = mfh.getMyCnfFile();
			
			if (lbs.isEnabled()) {
				mfh.disableBinLog();
				SSHcommonUtil.backupFile(session,server, mycnfFile); // 先备份配置文件， my.cnf -> my.cnf.1
				ScpUtil.to(session, mycnfFile, mfh.toByteArray());
				mysqlUtil.restartMysql(session, server); // 重启Mysql
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
		} catch (JSchException | IOException | RunRemoteCommandException | ScpException | AppNotStartedException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}
	}
	
	public MycnfFileHolder requestMysqlSettings(Server server) throws JSchException, IOException, AppNotStartedException, RunRemoteCommandException, UnExpectedOutputException, ScpException, MysqlAccessDeniedException, UnExpectedInputException {
		Session session = null;
		try {
			session = sshSessionFactory.getConnectedSession(server).getResult();
			return requestMysqlSettingsFromServer(session, server);
		} finally {
			if( session != null) {
				session.disconnect();
			}
		}
	}
	
	public MycnfFileHolder requestMysqlSettingsFromServer(Session session, Server server) throws JSchException, IOException, AppNotStartedException, RunRemoteCommandException, UnExpectedOutputException, ScpException, MysqlAccessDeniedException, UnExpectedInputException {
		MysqlVariables lbs = mysqlUtil.getLogbinState(session, server);
		MycnfFileHolder mfh = mysqlUtil.getMyCnfFile(session, server); // 找到起作用的my.cnf配置文件。
		mfh.setVariables(lbs.getMap());
		return mfh;
	}
	
	public MycnfFileHolder backupMysqlSettingsTolocalDisk(Session session, Server server) throws JSchException, IOException, AppNotStartedException, RunRemoteCommandException, UnExpectedOutputException, ScpException, MysqlAccessDeniedException, UnExpectedInputException {
//		Path mysqlSettingDir = settingsInDb.getLocalMysqlDir(server);
		MycnfFileHolder mfh = requestMysqlSettingsFromServer(session, server);
//		writeMysqlSettingsToDisk(server, mfh);
//		Files.write(mysqlSettingDir.resolve("mycnf.yml"), YamlInstance.INSTANCE.yaml.dumpAsMap(mfh).getBytes());
//		return mfh;
		
		Path mysqlSettingDir = settingsInDb.getLocalMysqlDir(server);
		String c = YamlInstance.INSTANCE.yaml.dumpAsMap(mfh);
		Path f = mysqlSettingDir.resolve(settingsInDb.getString("mysql.filenames.mycnf", "mycnf.yml"));
		Files.write(f, c.getBytes(StandardCharsets.UTF_8));
		return mfh;
		
	}
	
	public MycnfFileHolder getMysqlSettingsFromDisk(Server server) throws IOException {
		Path mysqlSettingDir = settingsInDb.getLocalMysqlDir(server);
		try (InputStream is = Files.newInputStream(mysqlSettingDir.resolve(settingsInDb.getString("mysql.filenames.mycnf", "mycnf.yml")))) {
			return YamlInstance.INSTANCE.yaml.loadAs(is, MycnfFileHolder.class);
		}
	}
	
//	private void writeMysqlSettingsToDisk(Server server, MycnfFileHolder mycnf) throws IOException {
//		Path mysqlSettingDir = settingsInDb.getLocalMysqlDir(server);
//		String c = YamlInstance.INSTANCE.yaml.dumpAsMap(mycnf);
//		Path f = mysqlSettingDir.resolve(settingsInDb.getString("mysql.filenames.mycnf", "mycnf.yml"));
//		Files.write(f, c.getBytes(StandardCharsets.UTF_8));
//	}
//	
	public MycnfFileHolder getMysqlSettingsFromDisk(Path mysqlcnf) throws IOException {
		try (InputStream is = Files.newInputStream(mysqlcnf)) {
			return YamlInstance.INSTANCE.yaml.loadAs(is, MycnfFileHolder.class);
		}
	}
	
	public void overWriteMycnf(Session session, Server server, MycnfFileHolder mfh) throws ScpException {
		ScpUtil.to(session, mfh.getMyCnfFile(), mfh.toByteArray());
	}

	public FacadeResult<?> enableLogbin(Session session, Server server) throws UnExpectedOutputException, MysqlAccessDeniedException, UnExpectedInputException {
		String logBinValue = MysqlVariables.LOG_BIN_BASENAME;
		if (server.getMysqlInstance() != null && server.getMysqlInstance().getLogBinSetting() != null) {
			logBinValue = server.getMysqlInstance().getLogBinSetting().getLogBinBasename();
		}
		return enableLogbin(session, server, logBinValue);
	}
	
	public FacadeResult<?> enableLogbin(Session session, Server server, String logBinValue) throws UnExpectedOutputException, MysqlAccessDeniedException, UnExpectedInputException {
		if (OsTypeWrapper.of(server.getOs()).isWin()) {
			return enableLogbinWin(session, server, logBinValue);
		} else {
			return enableLogbinCentos(session, server, logBinValue);
		}
		
	}
	
	public FacadeResult<?> enableLogbinCentos(Session session, Server server, String logBinValue) throws UnExpectedOutputException, MysqlAccessDeniedException, UnExpectedInputException {
		try {
			MysqlInstance mi = server.getMysqlInstance();
			
			MycnfFileHolder mfh = backupMysqlSettingsTolocalDisk(session, server);
			String mycnfFile = mfh.getMyCnfFile();
			MysqlVariables lbs = mfh.getMysqlVariables();
			
			if (!lbs.isEnabled()) {
				mfh.enableBinLog(logBinValue); // 修改logbin的值
				SSHcommonUtil.backupFile(session, server, mycnfFile); // 先备份配置文件， my.cnf -> my.cnf.1
				ScpUtil.to(session, mycnfFile, mfh.toByteArray());
				mysqlUtil.restartMysql(session, server); // 重启Mysql
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
	
	public FacadeResult<?> enableLogbinWin(Session session, Server server, String logBinValue) throws UnExpectedOutputException, MysqlAccessDeniedException, UnExpectedInputException {
		try {
			MysqlInstance mi = server.getMysqlInstance();
			
			MycnfFileHolder mfh = backupMysqlSettingsTolocalDisk(session, server);
			String mycnfFile = mfh.getMyCnfFile();
			MysqlVariables lbs = mfh.getMysqlVariables();
			
			if (!lbs.isEnabled()) {
				mfh.enableBinLog(logBinValue); // 修改logbin的值
				SSHcommonUtil.backupFile(session,server, mycnfFile); // 先备份配置文件， my.cnf -> my.cnf.1
				ScpUtil.to(session, mycnfFile, mfh.toByteArray());
				
				mysqlUtil.restartMysql(session, server); // 重启Mysql
				
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

	public FacadeResult<String> getMyCnfFile(Session session, Server server) throws UnExpectedOutputException, JSchException, IOException {
		try {
			return FacadeResult.doneExpectedResult(mysqlUtil.getEffectiveMyCnf(session, server), CommonActionResult.DONE);
		} catch (RunRemoteCommandException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}
	}

	public FacadeResult<?> getLogbinState(Session session, Server server) throws JSchException, IOException, MysqlAccessDeniedException, AppNotStartedException, UnExpectedInputException, UnExpectedOutputException {
	    return FacadeResult.doneExpectedResultDone(mysqlUtil.getLogbinState(session, server).toLines());
	}

	public FacadeResult<?> getMyCnf(Session session, Server server) throws RunRemoteCommandException, IOException, JSchException, ScpException, UnExpectedOutputException {
		MycnfFileHolder mfh = mysqlUtil.getMyCnfFile(session, server); // 找到起作用的my.cnf配置文件。
		return FacadeResult.doneExpectedResultDone(mfh.getMyCnfFile());

	}
	

	
	public CompletableFuture<AsyncTaskValue> restoreAsync(PlayBack playback, Server sourceServer, Server targetServer, String dumpFolder, String msgkey, Long id, boolean origin) throws IOException, JSchException, RunRemoteCommandException, UnExpectedOutputException, AppNotStartedException, ScpException {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return new AsyncTaskValue(id, restore(playback, sourceServer, targetServer, dumpFolder, origin)).withDescription(msgkey);
			} catch (RunRemoteCommandException | UnExpectedOutputException | IOException | JSchException
					| AppNotStartedException | ScpException | UnExpectedInputException | MysqlAccessDeniedException e1) {
				throw new ExceptionWrapper(e1);
			}
		}).exceptionally(e -> {
			Throwable throwable = e.getCause();
			if (throwable instanceof ExceptionWrapper) {
				ExceptionWrapper ew = (ExceptionWrapper) e.getCause();
				throwable = ew.getException();
			}
			return new AsyncTaskValue(id, throwable);
		});
	}
	
	public void resemblesOrigin(Server sourceServer, Server targetServer, Session targetSession) throws IOException, JSchException, ScpException, MysqlAccessDeniedException, AppNotStartedException, RunRemoteCommandException, UnExpectedOutputException, UnExpectedInputException {
		
		MycnfFileHolder mf = getMysqlSettingsFromDisk(sourceServer);
		String datadir = mf.getMysqlVariables().getDataDirEndNoPathSeparator();
		
		// if datadir exists in target server?
		boolean b = SSHcommonUtil.fileExists(targetServer.getOs(), targetSession, datadir);
		if (!b) {
			SSHcommonUtil.mkdirsp(targetServer.getOs(), targetSession, datadir);
		} else {
			SSHcommonUtil.backupFileByMove(targetSession, datadir);
		}
		
		mf.disableBinLog();
		
		String targetMycnfFile = mysqlUtil.getEffectiveMyCnf(targetSession, targetServer);
		
		byte[] bytes = String.join("\n", mf.getLines().toArray(new String[mf.getLines().size()])).getBytes(StandardCharsets.UTF_8);
		// override target my.cnf.
		ScpUtil.to(targetSession, targetMycnfFile, bytes);
		
		mysqlUtil.stopMysql(targetSession);
		mysqlUtil.restartMysql(targetSession, targetServer);
		mySqlInstaller.resetPassword(targetSession, targetServer, "", sourceServer.getMysqlInstance().getPassword());
		targetServer.getMysqlInstance().setPassword(sourceServer.getMysqlInstance().getPassword());
	}
	
	public Boolean restore(PlayBack playback, Server sourceServer, Server targetServer, String dumpFolder, boolean origin) throws IOException, JSchException, RunRemoteCommandException, UnExpectedOutputException, AppNotStartedException, ScpException, UnExpectedInputException, MysqlAccessDeniedException {
		Session targetSession = null;
		
		try {
			targetSession = sshSessionFactory.getConnectedSession(targetServer).getResult();
			if (origin) {
				resemblesOrigin(sourceServer, targetServer, targetSession);
			} else {
				FacadeResult<MysqlInstallInfo> fmi = mySqlInstaller.resetMysql(targetSession, targetServer, targetServer.getMysqlInstance().getPassword());
			}
			// now we got a brand new mysql instance.
			boolean b = restoreInternal(playback, sourceServer, targetServer, targetSession, dumpFolder);
			
			if (origin) {
				enableLogbin(targetSession, targetServer);
			}
			return b;
		} finally {
			if( targetSession != null) {
				targetSession.disconnect();
			}
		}
	}

	private Boolean restoreInternal(PlayBack playback, Server sourceServer, Server targetServer, Session targetSession, String dumpFolder) throws IOException, JSchException, RunRemoteCommandException, UnExpectedOutputException, AppNotStartedException, ScpException, UnExpectedInputException, MysqlAccessDeniedException {
			// remoteFolder contains all logbin files and name fixed dump file.
			String remoteFolder = uploadDumpFolder(sourceServer, targetServer, targetSession, dumpFolder);
//			String dumpfn = PathUtil.getFileName(sourceServer.getMysqlInstance().getDumpFileName());
//			dumpfn = PathUtil.join(remoteFolder, dumpfn);
			boolean importResult = importDumped(targetSession, targetServer, sourceServer.getMysqlInstance(), targetServer.getMysqlInstance(), remoteFolder);
			if (!importResult) {
				return false;
			}
			
			List<Path> binlogs = getLogBinFiles(sourceServer, dumpFolder);
			
			String remoteTmpSqlFile = PathUtil.join(remoteFolder, "tmp.sql");
//			mysqlbinlog binlog.000001 binlog.000002 > remoteTmpSqlFile
			String binlogJoined = binlogs.stream().map(bl -> PathUtil.join(remoteFolder, bl.getFileName().toString())).collect(Collectors.joining(" "));
			String rcmd = String.format("mysqlbinlog %s > %s", binlogJoined, remoteTmpSqlFile);
			
			RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(targetSession, rcmd);
			
			String sourceCmd = String.format("mysql -u root -p -e \"source %s\"", remoteTmpSqlFile);
			
			MysqlPasswordReadyExpect<List<String>> mpre = new MysqlPasswordReadyExpect<List<String>>(targetSession, targetServer) {
				@Override
				protected void invokeCommandWhichCausePasswordPrompt() throws IOException {
					expect.sendLine(sourceCmd);
				}
				
				@Override
				protected List<String> afterLogin() throws IOException {
					String raw = expectBashPromptAndReturnRaw(1, 1, TimeUnit.DAYS);
					return StringUtil.splitLines(raw.trim());
				}
			};
			List<String> resultLines = mpre.start();
			return resultLines != null && resultLines.size() == 1;
			
	}
	
	public String uploadDumpFolder(Server sourceServer, Server targetServer,Session targetSession, Path dumpFolder) throws IOException, JSchException {
		List<StorageState> ss = sss.getStorageState(targetServer, targetSession, false);
		Collections.sort(ss, StorageState.AVAILABLE_DESC);
		
		String remoteMaxRoot = ss.get(0).getRoot();
		String remoteDumpFolder = PathUtil.join(remoteMaxRoot, dumpFolder.getFileName().toString());
		SSHcommonUtil.deleteRemoteFolder(targetSession, remoteDumpFolder);
		SSHcommonUtil.copyFolder(targetServer.getOs(), targetSession, dumpFolder, remoteDumpFolder);
		return remoteDumpFolder;
	}
	
	public String uploadDumpFolder(Server sourceServer, Server targetServer,Session targetSession, String dumpFolder) throws IOException, JSchException {
		Path dumpFolderToUpload = settingsInDb.getDumpsDir(sourceServer).resolve(dumpFolder);
		return uploadDumpFolder(sourceServer, targetServer, targetSession, dumpFolderToUpload);
	}

	public List<MysqlDumpFolder> listDumpFolders(Server sourceServer) throws IOException {
		Path dumps = settingsInDb.getDumpsDir(sourceServer);
		List<MysqlDumpFolder> dumpFolders = Files.list(dumps).map(MysqlDumpFolder::newInstance).filter(Objects::nonNull).collect(Collectors.toList());
		Collections.sort(dumpFolders);
		return dumpFolders;
	}
	
	public CompletableFuture<Boolean> importDumpedAsync(Server targetServer, MysqlInstance sourceMysqlInstance,
			MysqlInstance targetMysqlInstance, String remoteFolder) throws UnExpectedOutputException {
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
			} catch (UnExpectedOutputException | MysqlAccessDeniedException e1) {
				throw new ExceptionWrapper(e1);
			} finally {
				if (targetSession != null && targetSession.isConnected()) {
					targetSession.disconnect();
				}
			}
		}).exceptionally(e -> {
			ExceptionUtil.logErrorException(logger, e);
			return false;
		});
	}
	
	
	
	private class PrefixAndPath {
		private String prefix;
		private Path file;
		
		public PrefixAndPath(String prefix, Path file) {
			super();
			this.prefix = prefix;
			this.file = file;
		}

		public String getPrefix() {
			return prefix;
		}
		public Path getFile() {
			return file;
		}
	}
	
	
	public List<Path> getLogBinFiles(Server server, String dumpFolder) throws IOException, UnExpectedInputException {
		Path dumpPath = settingsInDb.getDumpsDir(server).resolve(dumpFolder);
		return getLogBinFiles(dumpPath);
	}

	public List<Path> getLogBinFiles(Path dumpFolder) throws IOException, UnExpectedInputException {
		Pattern ptn = Pattern.compile("(.*)\\.(\\d+)$");
		Map<String, List<Path>> nameToFiles = Files.list(dumpFolder)
				.map(f -> {
					Matcher m = ptn.matcher(f.toString());
					if (m.matches()) {
						return new PrefixAndPath(m.group(1), f);
					} else {
						return null;
					}
				}).filter(Objects::nonNull).collect(Collectors.groupingBy(PrefixAndPath::getPrefix, Collectors.mapping(PrefixAndPath::getFile, Collectors.toList())));
		
		if (nameToFiles.size() == 0) {
			return Lists.newArrayList();
		}
		
		if (nameToFiles.size() != 2) {
			throw new UnExpectedInputException("1000", "dumpfolder.wrongformat", nameToFiles.size() + "");
		}
		List<String> names = new ArrayList<>(nameToFiles.keySet());
		
		String binName = names.get(0);
		String indexName = names.get(1);
		
		if (binName.length() > indexName.length()) {
			binName = names.get(1);
			indexName = names.get(0);
		}
		
		List<Path> indexFiles = nameToFiles.get(indexName);
		Collections.sort(indexFiles, PathUtil.PATH_NAME_DESC);
		
		List<String> lines = Files.readAllLines(indexFiles.get(0));
		
		if (lines.size() != nameToFiles.get(binName).size()) {
			throw new UnExpectedInputException("1000", "dumpfolder.wrongindex", lines.size() + "", nameToFiles.get(binName).size() + "");
		}
		
		List<Path> logbins = nameToFiles.get(binName);
		Collections.sort(logbins, PathUtil.PATH_NAME_ASC);
		return logbins;
	}
	
	public boolean importDumped(Session targetSession, Server targetServer, MysqlInstance sourceMysqlInstance,
			MysqlInstance targetMysqlInstance, String remoteFolder) throws UnExpectedOutputException, MysqlAccessDeniedException {
		//local dump file is fixed.
		String dumpfn = PathUtil.getFileName(MysqlUtil.FIXED_DUMP_FILE_NAME);
		dumpfn = PathUtil.join(remoteFolder, dumpfn);
		String cmd = String.format("mysql -uroot -p < %s", dumpfn);

		MysqlPasswordReadyExpect<List<String>> mpre = new MysqlPasswordReadyExpect<List<String>>(targetSession, targetServer) {
			
			@Override
			protected void invokeCommandWhichCausePasswordPrompt() throws IOException {
				expect.sendLine(cmd);
			}
			
			@Override
			protected List<String> afterLogin() throws IOException {
				String raw = expectBashPromptAndReturnRaw(1, 1, TimeUnit.DAYS);
				return StringUtil.splitLines(raw.trim());
			}
		};
		List<String> lines = mpre.start();
//		[root@localhost ~
		return lines.size() == 1;
	}
}
