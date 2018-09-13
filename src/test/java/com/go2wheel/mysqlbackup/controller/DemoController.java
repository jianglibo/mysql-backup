package com.go2wheel.mysqlbackup.controller;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.commands.MysqlService;
import com.go2wheel.mysqlbackup.exception.AppNotStartedException;
import com.go2wheel.mysqlbackup.exception.MysqlAccessDeniedException;
import com.go2wheel.mysqlbackup.exception.UnExpectedContentException;
import com.go2wheel.mysqlbackup.exception.UnExpectedInputException;
import com.go2wheel.mysqlbackup.installer.MySqlInstaller;
import com.go2wheel.mysqlbackup.installer.MysqlInstallInfo;
import com.go2wheel.mysqlbackup.model.MysqlInstance;
import com.go2wheel.mysqlbackup.model.RobocopyDescription;
import com.go2wheel.mysqlbackup.model.RobocopyItem;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.Software;
import com.go2wheel.mysqlbackup.service.MysqlInstanceDbService;
import com.go2wheel.mysqlbackup.service.RobocopyDescriptionDbService;
import com.go2wheel.mysqlbackup.service.RobocopyItemDbService;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.service.SoftwareDbService;
import com.go2wheel.mysqlbackup.util.SshSessionFactory;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@Controller
@RequestMapping("/app/demo")
public class DemoController {

	@Autowired
	private ServerDbService serverDbService;

	@Autowired
	private MysqlInstanceDbService mysqlInstanceDbService;

	@Autowired
	private MySqlInstaller mySqlInstaller;

	@Autowired
	protected SoftwareDbService softwareDbService;
	
	@Autowired
	private RobocopyItemDbService robocopyItemDbService;
	
	@Autowired
	private RobocopyDescriptionDbService robocopyDescriptionDbService;

	@Autowired
	protected SshSessionFactory sshSessionFactory;

	@Autowired
	private MysqlService mysqlService;

	@GetMapping("/mysql")
	public String createMysql() throws JSchException, UnExpectedContentException, MysqlAccessDeniedException, AppNotStartedException, UnExpectedInputException {
		Server server = serverDbService.findByHost(SpringBaseFort.HOST_DEFAULT_GET);

		if (server == null) {
			server = new Server(SpringBaseFort.HOST_DEFAULT_GET, "a server.");
			server.setServerRole("GET");
			server = serverDbService.save(server);
		}
		
		server = serverDbService.loadFull(server);
		
		if (server.getMysqlInstance() == null) {
			MysqlInstance mi = new MysqlInstance.MysqlInstanceBuilder(server.getId(), "123456").addSetting("log_bin", "ON")
					.addSetting("log_bin_basename", "/var/lib/mysql/hm-log-bin")
					.addSetting("log_bin_index", "/var/lib/mysql/hm-log-bin.index")
					.withFlushLogCron(SpringBaseFort.A_VALID_CRON_EXPRESSION).build();
			server.setMysqlInstance(mysqlInstanceDbService.save(mi));
		}

		String serverHost1 = "192.168.33.111";
		Server server1 = serverDbService.findByHost(serverHost1);
		if (server1 == null) {
			server1 = new Server(serverHost1, "b server.");
			server1.setServerRole("SET");
			server1 = serverDbService.save(server1);
		}
		
//		E:\wamp\bin\mysql\mysql5.5.24
		serverHost1 = "10.74.111.39";
		server1 = serverDbService.findByHost(serverHost1);
		if (server1 == null) {
			server1 = new Server(serverHost1, "c server.");
			server1.setServerRole(Server.ROLE_GET);
			server1 = serverDbService.save(server1);
		}
		
		server1 = serverDbService.loadFull(server1);
		
		if (server1.getMysqlInstance() == null) {
			MysqlInstance mi = new MysqlInstance.MysqlInstanceBuilder(server1.getId(), "q1w2e3r4").build();
			server1.setMysqlInstance(mysqlInstanceDbService.save(mi));
		}

		Session session = sshSessionFactory.getConnectedSession(server).getResult();

		List<Software> sfs = softwareDbService.findByName("MYSQL");
		Software software = sfs.get(0);
		MysqlInstallInfo ii = (MysqlInstallInfo) mySqlInstaller.install(session, server, software, "123456")
				.getResult();
		assertTrue(ii.isInstalled());
		mysqlService.enableLogbin(session, server);

		return "redirect:/";
	}
	
	@GetMapping("/robocopy")
	public String createLocalRobocopy() throws IOException  {
		Path src = Paths.get("c:/robosrc/src/a");
		Path src1 = Paths.get("c:/robosrc/src1/a");
		if (!Files.exists(src)) {
			Files.createDirectories(src);
		}
		if (!Files.exists(src1)) {
			Files.createDirectories(src1);
		}
		Files.write(src.resolve("a.txt"), "abc".getBytes());
		Files.write(src1.resolve("a.txt"), "abc".getBytes());
		
		Server ls = serverDbService.findByHost("localhost");
		if (ls == null) {
			ls = new Server("localhost", "benji");
			ls.setOs("win");
			ls.setServerRole(Server.ROLE_GET);
			ls = serverDbService.save(ls);
		}
		
		RobocopyDescription rd = robocopyDescriptionDbService.findByServerId(ls.getId());
		
		if (rd == null) {
			rd = new RobocopyDescription.RobocopyDescriptionBuilder(ls.getId(), "c:/roborepo").build();
			rd = robocopyDescriptionDbService.save(rd);
		}
		
		List<RobocopyItem> items = robocopyItemDbService.findByDescriptionId(rd.getId());
		if (items.size() == 0) {
			RobocopyItem ri = new RobocopyItem(rd.getId(), "c:/robosrc/src" , "src");
			RobocopyItem ri1 = new RobocopyItem(rd.getId(), "c:/robosrc/src1" , "src1");
			robocopyItemDbService.save(ri);
			robocopyItemDbService.save(ri1);
		}
		
		
		return "redirect:/";
	}
}
