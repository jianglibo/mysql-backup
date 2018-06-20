package com.go2wheel.mysqlbackup.service;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.quartz.JobExecutionException;

import com.go2wheel.mysqlbackup.model.MysqlInstance;
import com.go2wheel.mysqlbackup.model.Server;

public class TestMysqlInstanceDbService extends ServiceTbase {

	@Test
	public void t() throws JobExecutionException {
		Server server = createServer();
		
		MysqlInstance mi = new MysqlInstance.MysqlInstanceBuilder(server.getId(), "123456").addSetting("a", "b").build();
		
		mi = mysqlInstanceDbService.save(mi);

		List<MysqlInstance> mis = mysqlInstanceDbService.findAll();
		int sz = mis.size();
		assertThat(sz, equalTo(1));
		
		mi = mis.get(0);
		assertThat(mi.getMysqlSettings(), contains("a=b"));
		
		assertThat(mi.getLogBinSetting().getMap().get("a"), equalTo("b"));
	}

}
