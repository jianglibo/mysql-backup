package com.go2wheel.mysqlbackup.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.MyAppSettings;
import com.go2wheel.mysqlbackup.commands.BackupCommand;
import com.go2wheel.mysqlbackup.commands.BoxService;
import com.go2wheel.mysqlbackup.exception.MysqlAccessDeniedException;
import com.go2wheel.mysqlbackup.exception.MysqlNotStartedException;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.exception.ScpException;
import com.go2wheel.mysqlbackup.expect.MysqlInteractiveExpect;
import com.go2wheel.mysqlbackup.value.Box;
import com.go2wheel.mysqlbackup.value.Lines;
import com.go2wheel.mysqlbackup.value.LogBinSetting;
import com.go2wheel.mysqlbackup.value.MycnfFileHolder;
import com.go2wheel.mysqlbackup.value.MysqlInstanceYml;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@Component
public class MysqlUtil {

	public static final String MYSQL_PROMPT = "mysql> ";
	public static final String DUMP_FILE_NAME = "/tmp/mysqldump.sql";

	private MyAppSettings appSettings;
	
	@Autowired
	private BoxService boxService;

	public MycnfFileHolder getMyCnfFile(Session session, Box box)
			throws RunRemoteCommandException, IOException, JSchException, ScpException {
		String cnfFile = box.getMysqlInstance().getMycnfFile();
		if (!StringUtil.hasAnyNonBlankWord(cnfFile) || StringUtil.isNullString(cnfFile)) {
			box.getMysqlInstance().setMycnfFile(getEffectiveMyCnf(session, box));
			boxService.writeDescription(box);
		}
		String content = ScpUtil.from(session, box.getMysqlInstance().getMycnfFile()).toString();
		MycnfFileHolder mfh = new MycnfFileHolder(new ArrayList<>(StringUtil.splitLines(content)));
		return mfh;
	}

	public void restartMysql(Session session) throws RunRemoteCommandException {
		SSHcommonUtil.runRemoteCommand(session, "systemctl restart mysqld");
	}
	
	public void stopMysql(Session session) throws RunRemoteCommandException {
		SSHcommonUtil.runRemoteCommand(session, "systemctl stop mysqld");
	}


	public String getEffectiveMyCnf(Session session, Box box) throws RunRemoteCommandException {
		String matcherline = ".*Default options are read from the following.*";
		RemoteCommandResult result = SSHcommonUtil.runRemoteCommand(session, "mysql --help --verbose");
		Optional<String> possibleFiles = new Lines(result.getAllTrimedNotEmptyLines())
				.findMatchAndReturnNextLine(matcherline);
		List<String> filenames = Stream.of(possibleFiles.get().split("\\s+")).filter(l -> !l.trim().isEmpty())
				.collect(Collectors.toList());

		String command = "ls " + String.join(" ", filenames);
		return SSHcommonUtil.runRemoteCommand(session, command).getAllTrimedNotEmptyLines().stream()
				.filter(line -> line.indexOf("No such file or directory") == -1).findFirst().get();
	}
	
	public LogBinSetting getLogbinState(Session session, String username, String password) throws JSchException, IOException, MysqlAccessDeniedException, MysqlNotStartedException {
		return new MysqlInteractiveExpect<LogBinSetting>(session) {
			@Override
			protected LogBinSetting afterLogin() {
				Map<String, String> binmap = new HashMap<>();
				try {
					expect.sendLine("show variables like '%log_bin%';");
					List<String> result = expectMysqlPromptAndReturnList();
					binmap.put(LogBinSetting.LOG_BIN_VARIABLE, getColumnValue(result, LogBinSetting.LOG_BIN_VARIABLE, 0, 1));
					binmap.put(LogBinSetting.LOG_BIN_BASENAME,
							getColumnValue(result, LogBinSetting.LOG_BIN_BASENAME, 0, 1));
					binmap.put(LogBinSetting.LOG_BIN_INDEX, getColumnValue(result, LogBinSetting.LOG_BIN_INDEX, 0, 1));
				} catch (IOException e) {
				}
				return new LogBinSetting(binmap);
			}
		}.start(username, password);
	}

	public LogBinSetting getLogbinState(Session session, Box box) throws JSchException, IOException, MysqlAccessDeniedException, MysqlNotStartedException {
		return getLogbinState(session, box.getMysqlInstance().getUsername("root"), box.getMysqlInstance().getPassword());
	}
	
	public Map<String, String> getVariables(Session session, String username, String password, String...vnames) throws JSchException, IOException, MysqlAccessDeniedException, MysqlNotStartedException {
		return new MysqlInteractiveExpect<Map<String, String>>(session) {
			@Override
			protected Map<String, String> afterLogin() {
				Map<String, String> binmap = new HashMap<>();
				try {
					expect.sendLine("show variables;");
					List<String> result = expectMysqlPromptAndReturnList();
					
					for(String vname: vnames) {
						String v = getColumnValue(result, vname, 0, 1);
						if (!v.isEmpty()) {
							binmap.put(vname, v);
						}
					}

				} catch (IOException e) {
				}
				return binmap;
			}
		}.start(username, password);
	}
	
	public Map<String, String> getVariables(Session session, Box box, String...vnames) throws JSchException, IOException, MysqlAccessDeniedException, MysqlNotStartedException {
		return getVariables(session, box.getMysqlInstance().getUsername("root"), box.getMysqlInstance().getPassword(), vnames);
	}
	

//	public void writeDescription(Box box) throws IOException {
//		Path dstFile = null;
//		String ds = YamlInstance.INSTANCE.yaml.dumpAsMap(box);
//		Path dstDir = appSettings.getDataRoot().resolve(box.getHost());
//		if (!Files.exists(dstDir) || Files.isRegularFile(dstDir)) {
//			Files.createDirectories(dstDir);
//		}
//		dstFile = dstDir.resolve(BackupCommand.DESCRIPTION_FILENAME);
//		FileUtil.atomicWriteFile(dstFile, ds.getBytes());
//
//	}

	public void writeBinLog(Session session, Box box, String rfile) throws IOException, ScpException {
		Path dstDir = appSettings.getDataRoot().resolve(box.getHost()).resolve("logbin");
		if (!Files.exists(dstDir) || Files.isRegularFile(dstDir)) {
			Files.createDirectories(dstDir);
		}
		Path dstFile = dstDir.resolve(rfile);
		ScpUtil.from(session, rfile, dstFile.toAbsolutePath().toString());
	}

	public Path getDescriptionFile(Box instance) {
		return appSettings.getDataRoot().resolve(instance.getHost()).resolve(BackupCommand.DESCRIPTION_FILENAME);
	}

	@Autowired
	public void setAppSettings(MyAppSettings appSettings) {
		this.appSettings = appSettings;
	}
	
	public boolean tryPassword(Session session, String user, String password) {
		return false;
	}
	
	public MysqlInstallInfo getInstallInfo(Session session, Box box) throws RunRemoteCommandException, JSchException, IOException {
		MysqlInstallInfo mysqlInstallInfo = new MysqlInstallInfo();
		String cmd = "rpm -qa | grep mysql";
		
		RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, cmd);
		Optional<String> resultOp = rcr.getAllTrimedNotEmptyLines().stream().filter(li -> li.contains("community-release")).findAny();
		if (resultOp.isPresent()) {
			mysqlInstallInfo.setCommunityRelease(resultOp.get());
		}
		
		rcr = SSHcommonUtil.runRemoteCommand(session, "mysqld -V");

		if (rcr.isExitValueNotEqZero()) {
			mysqlInstallInfo.setInstalled(false);
		} else {
			mysqlInstallInfo.setInstalled(true);
			mysqlInstallInfo.setMysqlv(rcr.getAllTrimedNotEmptyLines().get(0));
			rcr = SSHcommonUtil.runRemoteCommand(session, "which mysqld");
			mysqlInstallInfo.setExecutable(rcr.getAllTrimedNotEmptyLines().get(0));
			
			cmd = String.format("rpm -qf %s", mysqlInstallInfo.getExecutable());
			rcr = SSHcommonUtil.runRemoteCommand(session, cmd);
			String line = rcr.getAllTrimedNotEmptyLines().get(0);
			mysqlInstallInfo.setPackageName(line);
			
			cmd = String.format("rpm -ql %s", mysqlInstallInfo.getPackageName());
			rcr = SSHcommonUtil.runRemoteCommand(session, cmd);
			mysqlInstallInfo.setRfiles(rcr.getAllTrimedNotEmptyLines());
			
			// this command need mysqld to be started, and know the password of the root.
			Map<String, String> variables = new HashMap<>();
			try {
				variables = getVariables(session, box, MysqlInstanceYml.VAR_DATADIR);
				mysqlInstallInfo.setVariables(variables);
			} catch (MysqlAccessDeniedException | MysqlNotStartedException e) {
				e.printStackTrace();
			}
			
		}
		return mysqlInstallInfo;
	}
	
	public static class MysqlInstallInfo {
		private boolean installed;
		
		private String executable;
		
		private String packageName;
		
		private String communityRelease;
		
		private List<String> rfiles;
		private String mysqlv;
		private Map<String, String> variables;
		
		public List<String> getRfiles() {
			return rfiles;
		}
		public void setRfiles(List<String> rfiles) {
			this.rfiles = rfiles;
		}
		public String getMysqlv() {
			return mysqlv;
		}
		public void setMysqlv(String mysqlv) {
			this.mysqlv = mysqlv;
		}
		public Map<String, String> getVariables() {
			return variables;
		}
		public void setVariables(Map<String, String> variables) {
			this.variables = variables;
		}
		public boolean isInstalled() {
			return installed;
		}
		public void setInstalled(boolean installed) {
			this.installed = installed;
		}
		public String getExecutable() {
			return executable;
		}
		public void setExecutable(String executable) {
			this.executable = executable;
		}
		public String getPackageName() {
			return packageName;
		}
		public void setPackageName(String packageName) {
			this.packageName = packageName;
		}
		public String getCommunityRelease() {
			return communityRelease;
		}
		public void setCommunityRelease(String communityRelease) {
			this.communityRelease = communityRelease;
		}
	}

}
