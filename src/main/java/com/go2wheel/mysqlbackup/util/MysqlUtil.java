package com.go2wheel.mysqlbackup.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.MyAppSettings;
import com.go2wheel.mysqlbackup.commands.BackupCommand;
import com.go2wheel.mysqlbackup.mysqlcfg.MyCnfFirstExist;
import com.go2wheel.mysqlbackup.mysqlcfg.MysqlCnfFileLister;
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
	
	private MyAppSettings appSettings;
	
	
	public MycnfFileHolder getMyCnfFile(Session session, Box box) throws IOException {
		if (box.getMysqlInstance().getMycnfFile() == null || box.getMysqlInstance().getMycnfFile().trim().isEmpty()) {
			box.getMysqlInstance().setMycnfFile(getEffectiveMyCnf(session, box));
			writeDescription(box);
		}
		String content = ScpUtil.from(session, box.getMysqlInstance().getMycnfFile()).toString(); 
		MycnfFileHolder mfh = new MycnfFileHolder(new ArrayList<>(StringUtil.splitLines(content)));
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
	
	public LogBinSetting getLogbinState(Session session, Box box) throws JSchException, IOException {
		return new MysqlInteractiveExpect<LogBinSetting>(session, box) {
			@Override
			protected LogBinSetting afterLogin() {
				Map<String, String> binmap = new HashMap<>();
				try {
					expect.sendLine("show variables like '%log_bin%';");
					List<String> result = expectMysqlPromptAndReturnList();
					result.stream().filter(line -> line.indexOf(LogBinSetting.LOG_BIN) != -1).forEach(System.out::println);
					binmap.put(LogBinSetting.LOG_BIN, getColumnValue(result, LogBinSetting.LOG_BIN, 0, 1));
					binmap.put(LogBinSetting.LOG_BIN_BASENAME, getColumnValue(result, LogBinSetting.LOG_BIN_BASENAME, 0, 1));
					binmap.put(LogBinSetting.LOG_BIN_INDEX, getColumnValue(result, LogBinSetting.LOG_BIN_INDEX, 0, 1));
				} catch (IOException e) {
				}
				return new LogBinSetting(binmap);
				
			}
		}.start();
	}
	
	public void flushLogs(Session session, Box box) {
		String content = ScpUtil.from(session, box.getMysqlInstance().getLogBinSetting().getLogBinIndex()).toString();
		List<String> logFiles = StringUtil.splitLines(content);
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
	
	
	public void writeBinLog(Session session, Box box, String rfile) throws IOException {
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
	
//	public DumpResult mysqldump(Session session, Box box) throws JSchException, IOException {
//		return new MysqlDumpExpect<DumpResult>(session, box) {
//			@Override
//			protected DumpResult afterLogin() {
//				return new DumpResult();
//			}
//		}.start();
//	}

	@Autowired
	public void setAppSettings(MyAppSettings appSettings) {
		this.appSettings = appSettings;
	}

}
