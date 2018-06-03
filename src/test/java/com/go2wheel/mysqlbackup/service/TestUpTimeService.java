package com.go2wheel.mysqlbackup.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.job.JobBaseFort;
import com.go2wheel.mysqlbackup.job.UpTimeJob;
import com.go2wheel.mysqlbackup.model.UpTime;

public class TestUpTimeService extends JobBaseFort {

	@Autowired
	private UpTimeJob upTimeJob;

	@Test
	public void t() throws SchedulerException {
		createServer();
		createContext();
		deleteAllJobs();
		
		upTimeJob.execute(context);

		List<UpTime> upTimes = upTimeService.getItemsInDays(server, 3);
		int sz = upTimes.size();
		assertThat(sz, equalTo(1));
		
		upTimes = upTimeService.findAll(com.go2wheel.mysqlbackup.jooqschema.tables.UpTime.UP_TIME.SERVER_ID.eq(server.getId()), 0, 10);
		sz = upTimes.size();
		assertThat(sz, equalTo(1));

		upTimeJob.execute(context);
		upTimes = upTimeService.getItemsInDays(server, 3);
		sz = upTimes.size();

		assertThat(sz, equalTo(2));
		
		upTimes = upTimeService.getRecentItems(30);
		assertThat(sz, equalTo(2));

	}

}
