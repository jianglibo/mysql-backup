package com.go2wheel.mysqlbackup.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.MyAppSettings;
import com.go2wheel.mysqlbackup.commands.BackupCommand;
import com.go2wheel.mysqlbackup.mysqlcfg.MyCnfFirstExist;
import com.go2wheel.mysqlbackup.mysqlcfg.MysqlCnfFileLister;
import com.go2wheel.mysqlbackup.util.MysqlDumpExpect.DumpResult;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.go2wheel.mysqlbackup.value.Box;
import com.go2wheel.mysqlbackup.value.LogBinSetting;
import com.go2wheel.mysqlbackup.value.MycnfFileHolder;
import com.go2wheel.mysqlbackup.yml.YamlInstance;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@Component
public class MysqlUtil {
	
	public static final String MYSQL_PROMPT = "mysql> ";
	
	public static Pattern MYSQL_HELP_MY_CNF = Pattern.compile(".*Default options are read from the following files in the given order:\\s*(.{10,}?)\\R+.*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	
	private MyAppSettings appSettings;
	
	
	public MycnfFileHolder enableLogBinOption(Session session, Box box, String logbinValue) throws IOException {
		if (box.getMysqlInstance().getMycnfFile() == null || box.getMysqlInstance().getMycnfFile().trim().isEmpty()) {
			box.getMysqlInstance().setMycnfFile(getEffectiveMyCnf(session, box));
			writeDescription(box);
		}
		String content = ScpUtil.from(session, box.getMysqlInstance().getMycnfFile()); 
		MycnfFileHolder mfh = new MycnfFileHolder(new ArrayList<>(StringUtil.splitLines(content)));
		mfh.enableBinLog(logbinValue);
		return mfh;
	}
	
	public void restartMysql(Session session) throws IOException, JSchException {
		SSHcommonUtil.runRemoteCommand(session, "systemctl restart mysqld");
	}
	
	public String getEffectiveMyCnf(Session session, Box box) {
		RemoteCommandResult<List<String>> er = ExecutorUtil.runListOfCommands(Arrays.asList(
				new MysqlCnfFileLister(session, box),
				new MyCnfFirstExist(session, box)));
		return er.getResult().get(0);	
	}

//	public MycnfFileHolder getMycnf(Box box) {
//		Session sshSession = sshClientFactory.getConnectedSession(box).get();
//		
//		RemoteCommandResult<List<String>> er = ExecutorUtil.runListOfCommands(Arrays.asList(
//				new MysqlCnfFileLister(sshSession, box),
//				new MyCnfFirstExist(sshSession, box)));
//		List<String> lines = er.getResult();
//		return new MycnfFileHolder(lines);
//	}
	
	public LogBinSetting getLogbinState(Session session, Box box) throws JSchException, IOException {
		return new MysqlExpect<LogBinSetting>(session, box) {
			@Override
			protected LogBinSetting afterLogin() {
				Map<String, String> binmap = new HashMap<>();
				try {
					expect.sendLine("show variables like '%log_bin%';");
					List<String> result = expectMysqlPromptAndReturnList();
					System.out.println("sssssssssssssssssss");
					result.stream().filter(line -> line.indexOf(LogBinSetting.LOG_BIN) != -1).forEach(System.out::println);
					binmap.put(LogBinSetting.LOG_BIN, getColumnValue(result, LogBinSetting.LOG_BIN_BASENAME, 0, 1));
					binmap.put(LogBinSetting.LOG_BIN_BASENAME, getColumnValue(result, LogBinSetting.LOG_BIN, 0, 1));
					binmap.put(LogBinSetting.LOG_BIN_INDEX, getColumnValue(result, LogBinSetting.LOG_BIN_INDEX, 0, 1));
				} catch (IOException e) {
				}
				return new LogBinSetting(binmap);
				
			}
		}.start();
	}
	
	public void writeDescription(Box box) throws IOException {
		String ds = YamlInstance.INSTANCE.getYaml().dumpAsMap(box);
		Path dstDir = appSettings.getDataRoot().resolve(box.getHost());
		if (!Files.exists(dstDir) || Files.isRegularFile(dstDir)) {
			Files.createDirectories(dstDir);
		}
		Path dstFile = dstDir.resolve(BackupCommand.DESCRIPTION_FILENAME);
		Files.write(dstFile, ds.getBytes());
	}
	
	public Path getDescriptionFile(Box instance) {
		return appSettings.getDataRoot().resolve(instance.getHost()).resolve(BackupCommand.DESCRIPTION_FILENAME);
	}
	
	public DumpResult mysqldump(Session session, Box box) throws JSchException, IOException {
		return new MysqlDumpExpect<DumpResult>(session, box) {
			@Override
			protected DumpResult afterLogin() {
				return new DumpResult();
			}
		}.start();
	}

	@Autowired
	public void setAppSettings(MyAppSettings appSettings) {
		this.appSettings = appSettings;
	}

}
