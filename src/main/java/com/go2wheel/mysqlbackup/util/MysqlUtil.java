package com.go2wheel.mysqlbackup.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.go2wheel.mysqlbackup.SettingsInDb;
import com.go2wheel.mysqlbackup.exception.AppNotStartedException;
import com.go2wheel.mysqlbackup.exception.MysqlAccessDeniedException;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.exception.ScpException;
import com.go2wheel.mysqlbackup.exception.UnExpectedContentException;
import com.go2wheel.mysqlbackup.expect.MysqlInteractiveExpect;
import com.go2wheel.mysqlbackup.expect.MysqlPasswordReadyExpect;
import com.go2wheel.mysqlbackup.installer.MysqlInstallInfo;
import com.go2wheel.mysqlbackup.model.MysqlInstance;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.value.Lines;
import com.go2wheel.mysqlbackup.value.MycnfFileHolder;
import com.go2wheel.mysqlbackup.value.MysqlVariables;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.google.common.collect.Lists;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@Component
public class MysqlUtil {

	public static final String MYSQL_PROMPT = "mysql> ";
	public static final String DUMP_FILE_NAME = "/tmp/mysqldump.sql";

	@Autowired
	private SettingsInDb settingsInDb;

	public MycnfFileHolder getMyCnfFile(Session session, Server server)
			throws RunRemoteCommandException, IOException, JSchException, ScpException, UnExpectedContentException {
		String cnfFile = getEffectiveMyCnf(session, server);
		String content = ScpUtil.from(session, cnfFile).toString();
		MycnfFileHolder mfh = new MycnfFileHolder(new ArrayList<>(StringUtil.splitLines(content)));
		mfh.setMyCnfFile(cnfFile);
		server.getMysqlInstance().setMycnfFile(cnfFile);
		return mfh;
	}

	public void restartMysql(Session session) throws RunRemoteCommandException {
		SSHcommonUtil.runRemoteCommand(session, "systemctl restart mysqld");
	}

	public void stopMysql(Session session) throws RunRemoteCommandException {
		SSHcommonUtil.runRemoteCommand(session, "systemctl stop mysqld");
	}

	public String getEffectiveMyCnf(Session session, Server server)
			throws RunRemoteCommandException, UnExpectedContentException {
		String matcherline = ".*Default options are read from the following.*";
		String cb = server.getMysqlInstance().getClientBin();
		String cmd = String.format("%smysql --help --verbose", cb == null ? "" : cb);
		RemoteCommandResult result = SSHcommonUtil.runRemoteCommand(session, cmd);
		Optional<String> possibleFiles = new Lines(result.getAllTrimedNotEmptyLines())
				.findMatchAndReturnNextLine(matcherline);
		if (!possibleFiles.isPresent()) {
			throw new UnExpectedContentException(null, null,
					result.getAllTrimedNotEmptyLines().stream().collect(Collectors.joining("\n")));
		}
		List<String> filenames = Stream.of(possibleFiles.get().split("\\s+")).filter(l -> !l.trim().isEmpty())
				.collect(Collectors.toList());

		String command = "ls " + String.join(" ", filenames);
		return SSHcommonUtil.runRemoteCommand(session, command).getAllTrimedNotEmptyLines().stream()
				.filter(line -> line.indexOf("No such file or directory") == -1).findFirst().get();
	}

	public MysqlVariables getLogbinState(Session session, String username, String password)
			throws JSchException, IOException, MysqlAccessDeniedException, AppNotStartedException {
		return new MysqlInteractiveExpect<MysqlVariables>(session) {
			@Override
			protected MysqlVariables afterLogin() {
				Map<String, String> binmap = new HashMap<>();
				try {
					expect.sendLine("show variables;");
					List<String> result = expectMysqlPromptAndReturnList();
					binmap = Arrays.asList(MysqlVariables.LOG_BIN_VARIABLE, MysqlVariables.LOG_BIN_BASENAME,
							MysqlVariables.LOG_BIN_INDEX, "innodb_version", "protocol_version", "version",
							"version_comment", "version_compile_machine", "version_compile_os", MysqlVariables.DATA_DIR)
							.stream().collect(Collectors.toMap(k -> k, k -> getColumnValue(result, k, 0, 1)));
					expect.sendLine("exit");
				} catch (IOException e) {
				}
				return new MysqlVariables(binmap);
			}
		}.start(username, password);
	}

	// innodb_version | 5.6.40 |
	// protocol_version | 10 |
	// slave_type_conversions | |
	// version | 5.6.40-log |
	// version_comment | MySQL Community Server (GPL) |
	// version_compile_machine | x86_64 |
	// version_compile_os | Linux |

	// public Map<String, String> getVersionInfo(Session session, String username,
	// String password)
	// throws JSchException, IOException, MysqlAccessDeniedException,
	// MysqlNotStartedException {
	// return new MysqlInteractiveExpect<Map<String, String>>(session) {
	// @Override
	// protected Map<String, String> afterLogin() {
	// Map<String, String> binmap = new HashMap<>();
	// try {
	// expect.sendLine("show variables like '%version%';");
	// List<String> result = expectMysqlPromptAndReturnList();
	// binmap = Arrays
	// .asList("innodb_version", "protocol_version", "slave_type_conversions",
	// "version_comment",
	// "version_compile_machine", "version_compile_os")
	// .stream().collect(Collectors.toMap(k -> k, k -> getColumnValue(result, k, 0,
	// 1)));
	// expect.sendLine("exit");
	// } catch (IOException e) {
	// }
	// return binmap;
	// }
	// }.start(username, password);
	// }

	public MysqlVariables getLogbinState(Session session, Server server)
			throws JSchException, IOException, AppNotStartedException, MysqlAccessDeniedException {
		return getLogbinState(session, server.getMysqlInstance().getUsername("root"),
				server.getMysqlInstance().getPassword());
	}

	public Map<String, String> getVariables(Session session, String username, String password, String... vnames)
			throws JSchException, IOException, MysqlAccessDeniedException, AppNotStartedException {
		return new MysqlInteractiveExpect<Map<String, String>>(session) {
			@Override
			protected Map<String, String> afterLogin() {
				Map<String, String> binmap = new HashMap<>();
				try {
					expect.sendLine("show variables;");
					List<String> result = expectMysqlPromptAndReturnList();

					for (String vname : vnames) {
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

	public Map<String, String> getVariables(Session session, Server server, String... vnames)
			throws JSchException, IOException, MysqlAccessDeniedException, AppNotStartedException {
		// if server had no mysqlInstance configuration, will cause null point
		// exception.
		if (server.getMysqlInstance() == null) {
			return null;
		}
		return getVariables(session, server.getMysqlInstance().getUsername("root"),
				server.getMysqlInstance().getPassword(), vnames);
	}

	public void writeBinLog(Session session, Server server, String rfile)
			throws IOException, ScpException, JSchException {
		Path dstDir = settingsInDb.getCurrentDumpDir(server);
		
		if (!Files.exists(dstDir) || Files.isRegularFile(dstDir)) {
			Files.createDirectories(dstDir);
		}
		Path dstFile = dstDir.resolve(rfile);
		ScpUtil.from(session, rfile, dstFile.toAbsolutePath().toString());
	}

	public boolean tryPassword(Session session, String user, String password) {
		return false;
	}

	public MysqlInstallInfo getInstallInfo(Session session, Server server)
			throws RunRemoteCommandException, JSchException, IOException, MysqlAccessDeniedException, AppNotStartedException {
		MysqlInstallInfo mysqlInstallInfo = new MysqlInstallInfo();
		String cmd = "rpm -qa | grep mysql";

		RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, cmd);
		Optional<String> resultOp = rcr.getAllTrimedNotEmptyLines().stream()
				.filter(li -> li.contains("community-release")).findAny();
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
			variables = getVariables(session, server, MysqlVariables.DATA_DIR);
			mysqlInstallInfo.setVariables(variables);

		}
		return mysqlInstallInfo;
	}

	public static String getColumnValue(List<String> lines, String key, int zeroBasedkeyColumn,
			int zeroBasedValueColumn) {
		int maxColumn = zeroBasedkeyColumn > zeroBasedValueColumn ? zeroBasedkeyColumn : zeroBasedValueColumn;
		StringBuffer ptbuf = new StringBuffer("\\s*");
		for (int i = 0; i <= maxColumn; i++) {
			if (i == zeroBasedkeyColumn) {
				ptbuf.append("\\|\\s+" + key + "\\s+");
			} else if (i == zeroBasedValueColumn) {
				ptbuf.append("\\|\\s+([^\\s]+)\\s+");
			} else {
				ptbuf.append("\\|\\s+[^\\s]+\\s+");
			}
		}
		ptbuf.append(".*");
		Pattern ptn = Pattern.compile(ptbuf.toString());
		return lines.stream().map(line -> ptn.matcher(line)).filter(m -> m.matches()).map(m -> m.group(1)).findFirst()
				.orElse("");
	}

	public static List<String> getColumnValues(List<String> lines, int zeroBasedValueColumn) {
		int maxColumn = zeroBasedValueColumn;
		StringBuffer ptbuf = new StringBuffer("\\s*");
		for (int i = 0; i <= maxColumn; i++) {
			if (i == zeroBasedValueColumn) {
				ptbuf.append("\\|\\s+([^\\|]+)");
			} else {
				ptbuf.append("\\|\\s+[^\\s]+\\s+");
			}
		}
		ptbuf.append(".*");
		Pattern ptn = Pattern.compile(ptbuf.toString());
		return lines.stream().map(line -> ptn.matcher(line)).filter(m -> m.matches()).map(m -> m.group(1).trim())
				.collect(Collectors.toList());
	}
	
	
	public static List<String> runSql(Session session, Server server, MysqlInstance mysqlInstance, String sql) throws UnExpectedContentException, MysqlAccessDeniedException {
		return (new MysqlPasswordReadyExpect(session, server) {
			@Override
			protected void tillPasswordRequired() throws IOException {
				String cmd = String.format("mysql -uroot -p -e \"%s\"", sql);
				expect.sendLine(cmd);
			}
			
			@Override
			protected List<String> afterLogin() throws IOException {
				String cnt = expectBashPromptAndReturnRaw(1, 1, TimeUnit.DAYS);
				List<String> names = MysqlUtil.getColumnValues(StringUtil.splitLines(cnt), 0);
				return names.subList(0, names.size()); 
			}
		}).start();
	}
	
	
	public static List<String> getDatabases(Session session, Server server, MysqlInstance mysqlInstance) throws UnExpectedContentException, MysqlAccessDeniedException {
		return (new MysqlPasswordReadyExpect(session, server) {
			@Override
			protected void tillPasswordRequired() throws IOException {
				String cmd = String.format("mysql -uroot -p -e \"%s\"", "show databases");
				expect.sendLine(cmd);
			}
			
			@Override
			protected List<String> afterLogin() throws IOException {
				String cnt = expectBashPromptAndReturnRaw(1, 1, TimeUnit.DAYS);
				List<String> names = MysqlUtil.getColumnValues(StringUtil.splitLines(cnt), 0);
				return names.subList(0, names.size()); 
			}
		}).start();
	}
	
	public static List<String> createDatabases(Session session, Server server, MysqlInstance mysqlInstance, String database) throws UnExpectedContentException, MysqlAccessDeniedException {
		return (new MysqlPasswordReadyExpect(session, server) {
			@Override
			protected void tillPasswordRequired() throws IOException {
				String cmd = String.format("mysql -uroot -p -e \"%s\"", String.format("create database %s charset utf8", database));
				expect.sendLine(cmd);
			}
			
			@Override
			protected List<String> afterLogin() throws IOException {
				expectBashPromptAndReturnRaw(1, 1, TimeUnit.DAYS);
				return Lists.newArrayList();
			}
		}).start();
	}
}
