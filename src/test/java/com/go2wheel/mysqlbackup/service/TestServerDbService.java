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

public class TestServerDbService extends ServiceTbase {
	
	
	@Test
	public void tWithMysqlInstanceAndBorgDescription() {
		Server server = new Server("abc", "bbc");
		server = serverDbService.save(server);
		
		MysqlInstance mi = new MysqlInstance.MysqlInstanceBuilder(server.getId(), "123456").build();
		mi = mysqlInstanceDbService.save(mi);
		
		BorgDescription bd = new BorgDescription.BorgDescriptionBuilder(server.getId()).build();
		bd = borgDescriptionDbService.save(bd);
		
		server = serverDbService.loadFull(server);
		
		assertNotNull(server.getMysqlInstance());
		assertNotNull(server.getBorgDescription());
	}
	
	@Test
	public void tCreate() {
		Server server = ca();
		server = serverDbService.save(server);
		
		assertThat(server.getId(), greaterThan(99));
		
		server = serverDbService.loadFull(server);
		
		assertNull(server.getMysqlInstance());
		assertNull(server.getBorgDescription());
	}
	
	@Test
	public void tFindByHost() {
		Server server = ca();
		assertThat(server.getId(), greaterThan(99));
		server = serverDbService.findByHost("abc");
		assertNotNull(server);
		
		server = serverDbService.findByHost("abckku");
		assertNull(server);
	}
	
	private Server ca() {
		Server server = new Server("abc", "bbc");
		return serverDbService.save(server);
	}
	
	@Test
	public void tFindLikeHost() {
		ca();
		List<Server> servers = serverDbService.findLikeHost("abc");
		assertThat(servers.size(), equalTo(1));
		
		servers = serverDbService.findLikeHost("a");
		assertThat(servers.size(), equalTo(1));
		
		servers = serverDbService.findLikeHost("b");
		assertThat(servers.size(), equalTo(1));
		
		servers = serverDbService.findLikeHost("c");
		assertThat(servers.size(), equalTo(1));
		
		servers = serverDbService.findLikeHost("ac");
		assertThat(servers.size(), equalTo(0));
		
		servers = serverDbService.findLikeHost("a%c");
		assertThat(servers.size(), equalTo(1));
		
		servers = serverDbService.findLikeHost("a_c");
		assertThat(servers.size(), equalTo(1));
	}


}
