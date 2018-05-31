package com.go2wheel.mysqlbackup.service;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.quartz.JobExecutionException;

import com.go2wheel.mysqlbackup.model.BorgDescription;
import com.go2wheel.mysqlbackup.model.Server;

public class TestBorgDescriptionService extends ServiceTbase {

	@Test
	public void t() throws JobExecutionException {
		Server server = createAServer();

		BorgDescription bd = new BorgDescription.BorgDescriptionBuilder(server.getId()).addInclude("/etc")
				.addInclude("/kk").addExclude("/e1").addExclude("/e2").build();
		
		bd = borgDescriptionService.save(bd);
		List<BorgDescription> bds = borgDescriptionService.findAll();
		int sz = bds.size();
		assertThat(sz, equalTo(1));

		assertThat(bds.get(0).getExcludes(), contains("/e1", "/e2"));
		assertThat(bds.get(0).getIncludes(), contains("/etc", "/kk"));
	}

}
