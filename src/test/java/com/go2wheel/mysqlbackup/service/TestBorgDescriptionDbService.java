package com.go2wheel.mysqlbackup.service;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

import com.go2wheel.mysqlbackup.model.BorgDescription;
import com.go2wheel.mysqlbackup.model.Server;

public class TestBorgDescriptionDbService extends ServiceTbase {

	@Test
	public void tCreate() {
		Server server = createServer();

		BorgDescription bd = new BorgDescription.BorgDescriptionBuilder(server.getId()).addInclude("/etc")
				.addInclude("/kk").addExclude("/e1").addExclude("/e2").build();
		
		bd = borgDescriptionDbService.save(bd);
		List<BorgDescription> bds = borgDescriptionDbService.findAll();
		int sz = bds.size();
		assertThat(sz, equalTo(1));

		assertThat(bds.get(0).getExcludes(), contains("/e1", "/e2"));
		assertThat(bds.get(0).getIncludes(), contains("/etc", "/kk"));
	}

}
