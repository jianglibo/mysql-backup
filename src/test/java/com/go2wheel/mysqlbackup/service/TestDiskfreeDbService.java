package com.go2wheel.mysqlbackup.service;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.job.DiskfreeJob;
import com.go2wheel.mysqlbackup.job.JobBaseFort;
import com.go2wheel.mysqlbackup.model.Diskfree;

public class TestDiskfreeDbService extends JobBaseFort {

	@Autowired
	private DiskfreeJob diskfreeJob;

	@Test
	public void t() throws JobExecutionException {
		diskfreeJob.execute(context);

		List<Diskfree> diskfrees = diskfreeDbService.getItemsInDays(server, 3);
		int sz = diskfrees.size();

		assertThat(sz, greaterThan(1));
		
		Date dt = diskfrees.get(0).getCreatedAt();
		
		assertFalse("all date field are same.", diskfrees.stream().anyMatch(d -> !d.getCreatedAt().equals(dt)));
		
		diskfreeJob.execute(context);
		
		diskfrees = diskfreeDbService.getItemsInDays(server, 3);
		assertTrue(diskfrees.size() == sz * 2);

	}

}
