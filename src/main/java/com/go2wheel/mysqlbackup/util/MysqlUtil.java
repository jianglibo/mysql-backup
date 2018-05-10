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
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.exception.ScpException;
import com.go2wheel.mysqlbackup.expect.MysqlInteractiveExpect;
import com.go2wheel.mysqlbackup.value.Box;
import com.go2wheel.mysqlbackup.value.Lines;
import com.go2wheel.mysqlbackup.value.LogBinSetting;
import com.go2wheel.mysqlbackup.value.MycnfFileHolder;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.go2wheel.mysqlbackup.yml.YamlInstance;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@Component
public class MysqlUtil {

	public static final String MYSQL_PROMPT = "mysql> ";
	public static final String DUMP_FILE_NAME = "/tmp/mysqldump.sql";

	private MyAppSettings appSettings;

	public MycnfFileHolder getMyCnfFile(Session session, Box box)
			throws RunRemoteCommandException, IOException, JSchException, ScpException {
		String cnfFile = box.getMysqlInstance().getMycnfFile();
		if (!StringUtil.hasAnyNonBlankWord(cnfFile)) {
			box.getMysqlInstance().setMycnfFile(getEffectiveMyCnf(session, box));
			writeDescription(box);
		}
		String content = ScpUtil.from(session, box.getMysqlInstance().getMycnfFile()).toString();
		MycnfFileHolder mfh = new MycnfFileHolder(new ArrayList<>(StringUtil.splitLines(content)));
		return mfh;
	}

	public void restartMysql(Session session) throws RunRemoteCommandException {
		SSHcommonUtil.runRemoteCommand(session, "systemctl restart mysqld");
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
		// RemoteCommandResult er = ExecutorUtil.runListOfCommands(
		// Arrays.asList(new MysqlCnfFileLister(session, box), new
		// MyCnfFirstExist(session, box)));
		// return er.getResult().get(0);
	}

	public LogBinSetting getLogbinState(Session session, Box box) throws JSchException, IOException {
		return new MysqlInteractiveExpect<LogBinSetting>(session, box) {
			@Override
			protected LogBinSetting afterLogin() {
				Map<String, String> binmap = new HashMap<>();
				try {
					expect.sendLine("show variables like '%log_bin%';");
					List<String> result = expectMysqlPromptAndReturnList();
					result.stream().filter(line -> line.indexOf(LogBinSetting.LOG_BIN_VARIABLE) != -1)
							.forEach(System.out::println);
					binmap.put(LogBinSetting.LOG_BIN_VARIABLE, getColumnValue(result, LogBinSetting.LOG_BIN_VARIABLE, 0, 1));
					binmap.put(LogBinSetting.LOG_BIN_BASENAME,
							getColumnValue(result, LogBinSetting.LOG_BIN_BASENAME, 0, 1));
					binmap.put(LogBinSetting.LOG_BIN_INDEX, getColumnValue(result, LogBinSetting.LOG_BIN_INDEX, 0, 1));
				} catch (IOException e) {
				}
				return new LogBinSetting(binmap);

			}
		}.start();
	}

	public void writeDescription(Box box) throws IOException {
		Path dstFile = null;
		String ds = YamlInstance.INSTANCE.getYaml().dumpAsMap(box);
		Path dstDir = appSettings.getDataRoot().resolve(box.getHost());
		if (!Files.exists(dstDir) || Files.isRegularFile(dstDir)) {
			Files.createDirectories(dstDir);
		}
		dstFile = dstDir.resolve(BackupCommand.DESCRIPTION_FILENAME);
		FileUtil.atomicWriteFile(dstFile, ds.getBytes());

	}

	public void writeBinLog(Session session, Box box, String rfile) throws IOException, ScpException {
		Path dstDir = appSettings.getDataRoot().resolve(box.getHost()).resolve("logbin");
		if (!Files.exists(dstDir) || Files.isRegularFile(dstDir)) {
			Files.createDirectories(dstDir);
		}
		Path dstFile = dstDir.resolve(rfile);
		ScpUtil.from(session, rfile, dstFile.toAbsolutePath().toString());
	}

	// public void writeBinLogIndex(Session session, Box box, List<String> lines)
	// throws IOException {
	// Path dstFile = getLogBinDir(box)
	// .resolve(Paths.get(box.getMysqlInstance().getLogBinSetting().getLogBinIndex()).getFileName());
	// Files.write(dstFile, String.join("\n", lines).getBytes());
	// }

	public Path getDescriptionFile(Box instance) {
		return appSettings.getDataRoot().resolve(instance.getHost()).resolve(BackupCommand.DESCRIPTION_FILENAME);
	}

	@Autowired
	public void setAppSettings(MyAppSettings appSettings) {
		this.appSettings = appSettings;
	}

//	private Path createTmpFile(String prefix, String postfix) {
//		return Files.createTempFile(prefix, postfix);
//	}

	// public void downloadDumped(Session session, Box box, LinuxFileInfo
	// linuxFileInfo) {
	// Path hd = appSettings.getDumpDir(box);
	// Path name = Paths.get(linuxFileInfo.getFilename()).getFileName();
	//
	// Path tmpFile = hd.resolve(name.toString() + ".downloading");
	// Path dst = hd.resolve(name.toString());
	//
	// ScpUtil.from(session, linuxFileInfo.getFilename(), tmpFile.toString());
	//
	// if
	// (!Md5Checksum.getMD5Checksum(tmpFile.toString()).equalsIgnoreCase(linuxFileInfo.getMd5()))
	// {
	// MysqlDumpException mde = new MysqlDumpException(box, "unmatched md5");
	// throw mde;
	// } else {
	// try {
	// Files.move(tmpFile, dst, StandardCopyOption.ATOMIC_MOVE);
	// } catch (IOException e) {
	// throw new MyCommonException("automicmove", e.getMessage());
	// }
	// }
	// }

}
