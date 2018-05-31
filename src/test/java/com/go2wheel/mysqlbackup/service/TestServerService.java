package com.go2wheel.mysqlbackup.service;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

import com.go2wheel.mysqlbackup.model.BorgDescription;
import com.go2wheel.mysqlbackup.model.MysqlInstance;
import com.go2wheel.mysqlbackup.model.Server;

public class TestServerService extends ServiceTbase {
	
	
	@Test
	public void tWithMysqlInstanceAndBorgDescription() {
		Server server = new Server("abc");
		server = serverService.save(server);
		
		MysqlInstance mi = new MysqlInstance.MysqlInstanceBuilder(server.getId(), "123456").build();
		mi = mysqlInstanceService.save(mi);
		
		BorgDescription bd = new BorgDescription.BorgDescriptionBuilder(server.getId()).build();
		bd = borgDescriptionService.save(bd);
		
		server = serverService.loadFull(server);
		
		assertNull(server.getMysqlInstance());
		assertNull(server.getBorgDescription());
	}
	
	@Test
	public void tCreate() {
		Server server = new Server("abc");
		server = serverService.save(server);
		
		assertThat(server.getId(), greaterThan(99));
		
		server = serverService.loadFull(server);
		
		assertNull(server.getMysqlInstance());
		assertNull(server.getBorgDescription());
	}
	
	@Test
	public void tFindByHost() {
		Server server = new Server("abc");
		server = serverService.save(server);
		assertThat(server.getId(), greaterThan(99));
		
		server = serverService.findByHost("abc");
		assertNotNull(server);
		
		server = serverService.findByHost("abckku");
		assertNull(server);
	}
	
	@Test
	public void tFindLikeHost() {
		Server server = new Server("abc");
		server = serverService.save(server);

		
		List<Server> servers = serverService.findLikeHost("abc");
		assertThat(servers.size(), equalTo(1));
		
		servers = serverService.findLikeHost("a");
		assertThat(servers.size(), equalTo(1));
		
		servers = serverService.findLikeHost("b");
		assertThat(servers.size(), equalTo(1));
		
		servers = serverService.findLikeHost("c");
		assertThat(servers.size(), equalTo(1));
		
		servers = serverService.findLikeHost("ac");
		assertThat(servers.size(), equalTo(0));
		
		servers = serverService.findLikeHost("a%c");
		assertThat(servers.size(), equalTo(1));
		
		servers = serverService.findLikeHost("a_c");
		assertThat(servers.size(), equalTo(1));
	}


}
