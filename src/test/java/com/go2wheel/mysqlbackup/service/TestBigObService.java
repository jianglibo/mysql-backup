package com.go2wheel.mysqlbackup.service;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import java.util.Date;

import org.junit.Test;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.job.JobBaseFort;
import com.go2wheel.mysqlbackup.model.BigOb;

public class TestBigObService extends JobBaseFort {

	@Autowired
	private BigObService bigObService;

	@Test
	public void t() throws SchedulerException {
		BigOb bo = new BigOb();
		bo.setContent("hhhhhhhhhhhhhhhhhhhhhh".getBytes());
		bo.setCreatedAt(new Date());
		bo.setDescription("description");
		bo.setName("hello");
		bo = bigObService.save(bo);
		assertThat(bo.getId(), greaterThan(99));
		
		int oid = bo.getId();
		
		bo = bigObService.findByName(bo.getName());
		
		assertThat(bo.getId(), equalTo(oid));
		
		

	}

}
