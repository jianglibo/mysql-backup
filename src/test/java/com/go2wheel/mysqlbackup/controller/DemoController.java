package com.go2wheel.mysqlbackup.controller;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.commands.MysqlService;
import com.go2wheel.mysqlbackup.exception.MysqlAccessDeniedException;
import com.go2wheel.mysqlbackup.exception.UnExpectedContentException;
import com.go2wheel.mysqlbackup.installer.MySqlInstaller;
import com.go2wheel.mysqlbackup.installer.MysqlInstallInfo;
import com.go2wheel.mysqlbackup.model.MysqlInstance;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.Software;
import com.go2wheel.mysqlbackup.service.MysqlInstanceDbService;
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
	protected SshSessionFactory sshSessionFactory;

	@Autowired
	private MysqlService mysqlService;

	@GetMapping("/mysql")
	public String createMysql() throws JSchException, UnExpectedContentException, MysqlAccessDeniedException {
		String serverHost1 = "192.168.33.111";
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

		Server server1 = serverDbService.findByHost(serverHost1);
		if (server1 == null) {
			server1 = new Server(serverHost1, "b server.");
			server1.setServerRole("SET");
			server1 = serverDbService.save(server1);
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
}
