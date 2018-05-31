package com.go2wheel.mysqlbackup.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.quartz.JobExecutionException;

import com.go2wheel.mysqlbackup.model.KeyValueInDb;

public class TestKeyValueInDbService extends ServiceTbase {


	@Test
	public void t() throws JobExecutionException {
		KeyValueInDb kv = KeyValueInDb.newMysqlKv(14, "a", "b");
		kv = keyValueInDbService.save(kv);

		List<KeyValueInDb> kvs = keyValueInDbService.findAll();
		int sz = kvs.size();
		assertThat(sz, equalTo(1));

		assertThat(kvs.get(0).getTheKey(), equalTo("a"));
		assertThat(kvs.get(0).getTheValue(), equalTo("b"));
	}

}
