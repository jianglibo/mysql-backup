package com.go2wheel.mysqlbackup.service;


import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.model.Server;

public class TestServerService extends ServiceTbase {
	
	@Autowired
	private ServerService service;
	
	@Before
	public void b() {
		deleteAll(service);
	}
	
	@Test
	public void t() {
		Server server = new Server("abc");
		server = service.save(server);
		assertThat(server.getId(), greaterThan(99));
	}

}
