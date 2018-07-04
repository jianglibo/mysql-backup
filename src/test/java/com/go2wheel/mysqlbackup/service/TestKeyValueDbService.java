package com.go2wheel.mysqlbackup.service;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.springframework.dao.DuplicateKeyException;

import com.go2wheel.mysqlbackup.job.JobBaseFort;
import com.go2wheel.mysqlbackup.model.KeyValue;

public class TestKeyValueDbService extends JobBaseFort {

	@Test
	public void tCreate() {
		clearDb();
		KeyValue kv = new  KeyValue("a", "b");
		kv = keyValueDbService.save(kv);
		assertThat(kv.getId(), greaterThan(99));
	}
	
	
	@Test
	public void tFindByKeyPrefix() {
		clearDb();
		KeyValue kv = new  KeyValue("a", "b");
		kv = keyValueDbService.save(kv);
		
		kv = new  KeyValue("a.b", "b");
		keyValueDbService.save(kv);
		kv = new  KeyValue("c.b", "b");
		keyValueDbService.save(kv);
		List<KeyValue> kvs = keyValueDbService.findByKeyPrefix("a");
		assertThat(kvs.size(), equalTo(2));
	}
	
	@Test(expected= DuplicateKeyException.class)
	public void tUnique() {
		clearDb();
		KeyValue kv = new  KeyValue("a", "b");
		kv = keyValueDbService.save(kv);
		kv = new  KeyValue("a", "b");
		kv = keyValueDbService.save(kv);
	}


}
