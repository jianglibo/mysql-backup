package com.go2wheel.mysqlbackup.service;


import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.go2wheel.mysqlbackup.model.Server;

public class TestServerService extends ServiceTbase {
	
	@Test
	public void t() {
		Server server = new Server("abc");
		server = serverService.save(server);
		assertThat(server.getId(), greaterThan(99));
	}

}
