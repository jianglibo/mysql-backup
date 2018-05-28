package com.go2wheel.mysqlbackup.service;


import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.DuplicateKeyException;

import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.ServerGrp;

public class TestServerGrpService extends ServiceTbase {
	
	@Before
	public void b() {
		deleteAll(serverGrpService);
	}
	
	@Test
	public void testInsertSuccess() {
		ServerGrp serverGrp = new ServerGrp("abc1");
		serverGrp = serverGrpService.save(serverGrp);
		assertThat(serverGrp.getId(), greaterThan(99));
	}
	
	@Test(expected = DuplicateKeyException.class)
	public void testInsertViolateUniqueName() {
		ServerGrp serverGrp = new ServerGrp("abc1");
		
		serverGrpService.save(serverGrp);
		serverGrp = serverGrpService.save(serverGrp);
		
		assertThat(serverGrp.getId(), greaterThan(99));
	}
	
	@Test
	public void addServer() {
		ServerGrp serverGrp = new ServerGrp("abc1");
		serverGrp = serverGrpService.save(serverGrp);
		
		Server server = new Server("abc");
		server = serverService.save(server);
		
		List<Server> servers = serverGrpService.getServers(serverGrp);
		
		assertThat(servers.size(), equalTo(0));
		
		serverGrpService.addServer(serverGrp, server);
		
		servers = serverGrpService.getServers(serverGrp);
		assertThat(servers.size(), equalTo(1));
		
		serverGrpService.removeServer(serverGrp, server);
		
		servers = serverGrpService.getServers(serverGrp);
		assertThat(servers.size(), equalTo(0));

	}
	
	
	public void tFindLikeEname() {
		ServerGrp serverGrp = new ServerGrp("abc1");
		serverGrp = serverGrpService.save(serverGrp);
		
		List<ServerGrp> sgps = serverGrpService.findLikeEname("a");
		assertThat(sgps.size(), equalTo(1));
		
		sgps = serverGrpService.findLikeEname("b");
		assertThat(sgps.size(), equalTo(1));
		
		sgps = serverGrpService.findLikeEname("c");
		assertThat(sgps.size(), equalTo(1));
		
		sgps = serverGrpService.findLikeEname("1");
		assertThat(sgps.size(), equalTo(1));




	}


}
