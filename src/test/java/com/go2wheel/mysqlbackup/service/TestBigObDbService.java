package com.go2wheel.mysqlbackup.service;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import java.util.Date;

import org.junit.Test;

import com.go2wheel.mysqlbackup.job.JobBaseFort;
import com.go2wheel.mysqlbackup.model.BigOb;

public class TestBigObDbService extends JobBaseFort {

	@Test
	public void tCreate() {
		clearDb();
		BigOb bo = new BigOb();
		bo.setContent("hhhhhhhhhhhhhhhhhhhhhh".getBytes());
		bo.setCreatedAt(new Date());
		bo.setDescription("description");
		bo.setName("hello");
		bo = bigObDbService.save(bo);
		assertThat(bo.getId(), greaterThan(99));
		
		int oid = bo.getId();
		
		bo = bigObDbService.findByName(bo.getName());
		
		assertThat(bo.getId(), equalTo(oid));
		
		

	}

}
