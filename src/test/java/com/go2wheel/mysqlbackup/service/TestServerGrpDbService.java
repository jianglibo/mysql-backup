package com.go2wheel.mysqlbackup.service;


import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.springframework.dao.DuplicateKeyException;

import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.ServerGrp;

public class TestServerGrpDbService extends ServiceTbase {
	
	@Test
	public void testInsertSuccess() {
		ServerGrp serverGrp = new ServerGrp("abc1");
		serverGrp = serverGrpDbService.save(serverGrp);
		assertThat(serverGrp.getId(), greaterThan(99));
	}
	
	@Test(expected = DuplicateKeyException.class)
	public void testInsertViolateUniqueName() {
		ServerGrp serverGrp = new ServerGrp("abc1");
		
		serverGrpDbService.save(serverGrp);
		serverGrp = serverGrpDbService.save(serverGrp);
		
		assertThat(serverGrp.getId(), greaterThan(99));
	}
	
	@Test
	public void addServer() {
		ServerGrp serverGrp = new ServerGrp("abc1");
		serverGrp = serverGrpDbService.save(serverGrp);
		
		Server server = new Server("abc", "bbc");
		server = serverDbService.save(server);
		
		List<Server> servers = serverGrpDbService.getServers(serverGrp);
		
		assertThat(servers.size(), equalTo(0));
		
		serverGrpDbService.addServer(serverGrp, server);
		
		servers = serverGrpDbService.getServers(serverGrp);
		assertThat(servers.size(), equalTo(1));
		
		serverGrpDbService.removeServer(serverGrp, server);
		
		servers = serverGrpDbService.getServers(serverGrp);
		assertThat(servers.size(), equalTo(0));

	}
	
	
	public void tFindLikeEname() {
		ServerGrp serverGrp = new ServerGrp("abc1");
		serverGrp = serverGrpDbService.save(serverGrp);
		
		List<ServerGrp> sgps = serverGrpDbService.findLikeEname("a");
		assertThat(sgps.size(), equalTo(1));
		
		sgps = serverGrpDbService.findLikeEname("b");
		assertThat(sgps.size(), equalTo(1));
		
		sgps = serverGrpDbService.findLikeEname("c");
		assertThat(sgps.size(), equalTo(1));
		
		sgps = serverGrpDbService.findLikeEname("1");
		assertThat(sgps.size(), equalTo(1));




	}


}
