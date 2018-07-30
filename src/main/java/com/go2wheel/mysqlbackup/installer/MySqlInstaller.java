package com.go2wheel.mysqlbackup.installer;

import static net.sf.expectit.matcher.Matchers.contains;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.exception.ScpException;
import com.go2wheel.mysqlbackup.http.FileDownloader;
import com.go2wheel.mysqlbackup.model.MysqlInstance;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.Software;
import com.go2wheel.mysqlbackup.model.SoftwareInstallation;
import com.go2wheel.mysqlbackup.mysqlinstaller.MysqlYumRepo;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.util.MysqlUtil;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.util.ScpUtil;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.value.ConfigValue;
import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.go2wheel.mysqlbackup.value.FacadeResult.CommonActionResult;
import com.google.common.collect.Lists;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import net.sf.expectit.Expect;
import net.sf.expectit.ExpectBuilder;
import net.sf.expectit.ExpectIOException;

@Service
public class MySqlInstaller extends InstallerBase<MysqlInstallInfo> {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private static final String MYSQL_COMMUNITY_RELEASE_BINARY_URL = "https://dev.mysql.com/get/mysql57-community-release-el7-11.noarch.rpm";

	public static final String MYSQL_REPO = "/etc/yum.repos.d/mysql-community.repo";

	public static final String[] SUPPORTED_VERSIONS = new String[] { "55", "56", "57", "80" };

	private MysqlUtil mysqlUtil;

	@Autowired
	private SshSessionFactory sshSessionFactory;

	public FacadeResult<MysqlInstallInfo> install(Session session, Server server, Software software,
			String initPassword) {
		try {
			if (!Stream.of(SUPPORTED_VERSIONS).anyMatch(v -> v.equals(software.getVersion()))) {
				return FacadeResult.unexpectedResult(String.format("unsupported version: %s", software.getVersion()));
			}
			String remote_file = "/tmp/" + StringUtil.getLastPartOfUrl(MYSQL_COMMUNITY_RELEASE_BINARY_URL);

			// if didn't install mysql yet, how can I get the info?
			MysqlInstallInfo info = mysqlUtil.getInstallInfo(session, server);

			if (!info.isInstalled()) {
				Path localPath = getLocalInstallerPath(software);
				ScpUtil.to(session, localPath.toString(), remote_file);
				String command = String.format("rpm -Uvh %s", remote_file);

				SSHcommonUtil.runRemoteCommand(session, command);

				String result = new String(ScpUtil.from(session, MYSQL_REPO).toByteArray());
				MysqlYumRepo myp = new MysqlYumRepo(StringUtil.splitLines(result));
				// mysql55-community, mysql56-community, mysql57-community, mysql80-community
				Stream.of(SUPPORTED_VERSIONS).map(v -> String.format("mysql%s-community", v)).forEach(b -> {
					ConfigValue cv = myp.getConfigValue(b, "enabled");
					myp.setConfigValue(cv, "0");
				});

				Optional<ConfigValue> cv = Stream.of(SUPPORTED_VERSIONS).filter(v -> software.getVersion().equals(v))
						.map(v -> String.format("mysql%s-community", v)).map(b -> myp.getConfigValue(b, "enabled"))
						.findFirst();

				myp.setConfigValue(cv.get(), "1");

				result = String.join("\n", myp.getLines());
				ScpUtil.to(session, MYSQL_REPO, result.getBytes());

				command = String.format("yum install -y %s", "mysql-community-server");
				
				SSHcommonUtil.runRemoteCommand(session, command);

				command = "systemctl start mysqld";

				SSHcommonUtil.runRemoteCommand(session, command);

				command = "mysql_secure_installation";

				Channel channel = session.openChannel("shell");
				channel.connect();

				Expect expect;
				// @formatter:off
				expect = new ExpectBuilder()
						.withOutput(channel.getOutputStream())
						.withInputs(channel.getInputStream(), channel.getExtInputStream())
						.withEchoOutput(System.out)
						.withEchoInput(System.err)
						.withExceptionOnFailure().build();
				try {
					expect.sendLine(command);
					expect.expect(contains("(enter for none):"));
					expect.sendLine();
					
					try {
						expect.withTimeout(500, TimeUnit.MILLISECONDS).expect(contains("Access denied"));
						return FacadeResult.unexpectedResult("执行mysql_secure_installation失败，密码错误，可能原来安装的文件尚在�??");
					} catch (ExpectIOException e) {
					}
					
					expect.expect(contains("[Y/n] "));
					expect.sendLine("Y");
					
					expect.expect(contains("New password:"));
					expect.sendLine(initPassword);
					
					expect.expect(contains("Re-enter new password:"));
					expect.sendLine(initPassword);

					expect.expect(contains("[Y/n] "));
					expect.sendLine("Y");
					
					expect.expect(contains("[Y/n] "));
					expect.sendLine("Y");
					
					expect.expect(contains("[Y/n] "));
					expect.sendLine("Y");
					
				} finally {
					expect.close();
					channel.disconnect();
					expect = null;
				}
				info = mysqlUtil.getInstallInfo(session, server);
				SoftwareInstallation si = SoftwareInstallation.newInstance(server, software);
				softwareInstallationDbService.save(si);
			}
			return FacadeResult.doneExpectedResult(info, CommonActionResult.DONE);
		} catch (RunRemoteCommandException | JSchException | IOException | ScpException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}
	}

	public FacadeResult<MysqlInstallInfo> unInstall(Session session, Server server, Software software) {
		try {
			MysqlInstallInfo info = mysqlUtil.getInstallInfo(session, server);
			if (!info.isInstalled()) {
				return FacadeResult.doneExpectedResult(info, CommonActionResult.DONE);
			}
			mysqlUtil.stopMysql(session);
			String cmd = String.format("yum -y remove %s", info.getCommunityRelease());
			SSHcommonUtil.runRemoteCommand(session, cmd);
			cmd = String.format("yum -y remove %s", info.getPackageName());
			SSHcommonUtil.runRemoteCommand(session, cmd);

			String datadir = info.getVariables().get(MysqlInstance.VAR_DATADIR);
			SSHcommonUtil.backupFileByMove(session, datadir);
			if (datadir != null) {
				cmd = String.format("rm -rf %s", datadir);
				SSHcommonUtil.runRemoteCommand(session, cmd);
			}
			
			return FacadeResult.doneExpectedResult(mysqlUtil.getInstallInfo(session, server), CommonActionResult.DONE);
		} catch (RunRemoteCommandException | JSchException | IOException e) {
			ExceptionUtil.logErrorException(logger, e);
			return FacadeResult.unexpectedResult(e);
		}
	}

	@Autowired
	public void setFileDownloader(FileDownloader fileDownloader) {
		this.fileDownloader = fileDownloader;
	}

	@Autowired
	public void setMysqlUtil(MysqlUtil mysqlUtil) {
		this.mysqlUtil = mysqlUtil;
	}

	@Override
	public FacadeResult<MysqlInstallInfo> install(Server server, Software software, Map<String, String> parasMap) throws JSchException {
		Session session = sshSessionFactory.getConnectedSession(server).getResult();
		FacadeResult<MysqlInstallInfo> fr = install(session, server, software, parasMap.get("initPassword"));
		return fr;
	}
	
	@PostConstruct
	public void saveInstaller() {
		syncToDb();
	}

	@Override
	public CompletableFuture<FacadeResult<MysqlInstallInfo>> installAsync(Server server, Software software, Map<String, String> parasMap) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return install(server, software, parasMap);
			} catch (JSchException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return FacadeResult.unexpectedResult(e);
			}
		});
	}

	@Override
	public boolean canHandle(Software software) {
		return software.getName().equals("MYSQL");
	}

	@Override
	public void syncToDb() {
			Software software = new Software();
			software.setName("MYSQL");
			software.setVersion("56");
			software.setTargetEnv("linux_centos");
			software.setDlurl(MYSQL_COMMUNITY_RELEASE_BINARY_URL);
			software.setInstaller("mysql57-community-release-el7-11.noarch.rpm");
			List<String> ls = Lists.newArrayList();
			ls.add("initPassword=123456");
			software.setSettings(ls);
			try {
				saveToDb(software);
			} catch (Exception e) {
			}
			
			software = new Software();
			software.setName("MYSQL");
			software.setVersion("57");
			software.setTargetEnv("linux_centos");
			software.setDlurl(MYSQL_COMMUNITY_RELEASE_BINARY_URL);
			software.setInstaller("mysql57-community-release-el7-11.noarch.rpm");
			
			ls = Lists.newArrayList();
			ls.add("initPassword=123456");
			software.setSettings(ls);
			try {
				saveToDb(software);
			} catch (Exception e) {
			}
			Path local = settingsInDb.getDownloadPath().resolve(software.getInstaller());
			
			fileDownloader.downloadAsync(software.getDlurl(), local).exceptionally(throwable -> {
				try {
					Files.delete(local);
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			});
	}

	@Override
	public FacadeResult<MysqlInstallInfo> uninstall(Server server, Software software) throws JSchException {
		return unInstall(getSession(server), server, software);
	}

	@Override
	public CompletableFuture<FacadeResult<MysqlInstallInfo>> uninstallAsync(Server server, Software software) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return uninstall(server, software);
			} catch (JSchException e) {
				e.printStackTrace();
				return FacadeResult.unexpectedResult(e);
			}
		});
	}

}
