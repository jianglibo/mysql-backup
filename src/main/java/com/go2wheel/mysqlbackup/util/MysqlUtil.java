package com.go2wheel.mysqlbackup.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.MyAppSettings;
import com.go2wheel.mysqlbackup.commands.BackupCommand;
import com.go2wheel.mysqlbackup.exception.DescriptionFileWriteException;
import com.go2wheel.mysqlbackup.exception.MyCommonException;
import com.go2wheel.mysqlbackup.exception.MysqlDumpException;
import com.go2wheel.mysqlbackup.expect.MysqlInteractiveExpect;
import com.go2wheel.mysqlbackup.mysqlcfg.MyCnfFirstExist;
import com.go2wheel.mysqlbackup.mysqlcfg.MysqlCnfFileLister;
import com.go2wheel.mysqlbackup.util.StringUtil.LinuxFileInfo;
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
		RemoteCommandResult<List<String>> er = ExecutorUtil.runListOfCommands(
				Arrays.asList(new MysqlCnfFileLister(session, box), new MyCnfFirstExist(session, box)));
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
					result.stream().filter(line -> line.indexOf(LogBinSetting.LOG_BIN) != -1)
							.forEach(System.out::println);
					binmap.put(LogBinSetting.LOG_BIN, getColumnValue(result, LogBinSetting.LOG_BIN, 0, 1));
					binmap.put(LogBinSetting.LOG_BIN_BASENAME,
							getColumnValue(result, LogBinSetting.LOG_BIN_BASENAME, 0, 1));
					binmap.put(LogBinSetting.LOG_BIN_INDEX, getColumnValue(result, LogBinSetting.LOG_BIN_INDEX, 0, 1));
				} catch (IOException e) {
				}
				return new LogBinSetting(binmap);

			}
		}.start();
	}

	public void writeDescription(Box box) {
		Path dstFile = null;
		try {
			String ds = YamlInstance.INSTANCE.getYaml().dumpAsMap(box);
			Path dstDir = appSettings.getDataRoot().resolve(box.getHost());
			if (!Files.exists(dstDir) || Files.isRegularFile(dstDir)) {
				Files.createDirectories(dstDir);
			}
			dstFile = dstDir.resolve(BackupCommand.DESCRIPTION_FILENAME);
			Path tmpFile = Files.createTempFile("tmpdescriptionfile", "yml");
			Files.write(tmpFile, ds.getBytes());
			Files.move(tmpFile, dstFile, StandardCopyOption.ATOMIC_MOVE);
		} catch (IOException e) {
			throw new DescriptionFileWriteException(box, dstFile);
		}
	}

	public void writeBinLog(Session session, Box box, String rfile) throws IOException {
		Path dstDir = appSettings.getDataRoot().resolve(box.getHost()).resolve("logbin");
		if (!Files.exists(dstDir) || Files.isRegularFile(dstDir)) {
			Files.createDirectories(dstDir);
		}
		Path dstFile = dstDir.resolve(rfile);
		ScpUtil.from(session, rfile, dstFile.toAbsolutePath().toString());
	}

	private Path getHostDir(Box box) {
		return appSettings.getDataRoot().resolve(box.getHost());
	}

	private Path getLogBinDir(Box box) throws IOException {
		Path dstDir = getHostDir(box).resolve("logbin");
		if (!Files.exists(dstDir) || Files.isRegularFile(dstDir)) {
			Files.createDirectories(dstDir);
		}
		return dstDir;
	}

	private Path getDumpDir(Box box) {
		try {
			Path dstDir = getHostDir(box).resolve("dump");
			if (!Files.exists(dstDir) || Files.isRegularFile(dstDir)) {
				Files.createDirectories(dstDir);
			}
			return dstDir;
		} catch (IOException e) {
			throw new MysqlDumpException(box, "create dump folder failed.");
		}
	}

	public void writeBinLogIndex(Session session, Box box, List<String> lines) throws IOException {
		Path dstFile = getLogBinDir(box)
				.resolve(Paths.get(box.getMysqlInstance().getLogBinSetting().getLogBinIndex()).getFileName());
		Files.write(dstFile, String.join("\n", lines).getBytes());
	}

	public Path getDescriptionFile(Box instance) {
		return appSettings.getDataRoot().resolve(instance.getHost()).resolve(BackupCommand.DESCRIPTION_FILENAME);
	}

	@Autowired
	public void setAppSettings(MyAppSettings appSettings) {
		this.appSettings = appSettings;
	}

	private Path createTmpFile(String prefix, String postfix) {
		try {
			return Files.createTempFile(prefix, postfix);
		} catch (IOException e) {
			throw new MyCommonException("tmpfile", e.getMessage());
		}
	}
	


	public void downloadDumped(Session session, Box box, LinuxFileInfo linuxFileInfo) {
		Path hd = getDumpDir(box);
		Path name = Paths.get(linuxFileInfo.getFilename()).getFileName();

		Path tmpFile = hd.resolve(name.toString() + ".downloading");
		Path dst = hd.resolve(name.toString());

		ScpUtil.from(session, linuxFileInfo.getFilename(), tmpFile.toString());

		if (!Md5Checksum.getMD5Checksum(tmpFile.toString()).equalsIgnoreCase(linuxFileInfo.getMd5())) {
			MysqlDumpException mde = new MysqlDumpException(box, "unmatched md5");
			throw mde;
		} else {
			try {
				Files.move(tmpFile, dst, StandardCopyOption.ATOMIC_MOVE);
			} catch (IOException e) {
				throw new MyCommonException("automicmove", e.getMessage());
			}
		}
	}

}
