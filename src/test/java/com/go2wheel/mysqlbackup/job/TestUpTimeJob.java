package com.go2wheel.mysqlbackup.job;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.model.Server;

public class TestUpTimeJob extends JobBaseFort {
	
	@Autowired
	private UpTimeJob upTimeJob;
	
	@Test
	public void t() throws SchedulerException {
		createServer();
		deleteAllJobs();
		createContext();
		upTimeJob.execute(context);
		
		upTimeDbService.count();
		assertThat(upTimeDbService.count(), equalTo(1L));
	}
	
	@Test
	public void tCreateServerWithUptimeCron() throws SchedulerException {
		server = new Server(HOST_DEFAULT, "a server.");
		server = serverDbService.save(server);
		printJobs();
		assertThat(countJobs(), equalTo(0L)); // cause server's uptimeCron and diskfreeCron both are null.
		serverDbService.delete(server);
		
		
		server = new Server(HOST_DEFAULT, "a server.");
		server.setUptimeCron(A_VALID_CRON_EXPRESSION);
		server.setDiskfreeCron(A_VALID_CRON_EXPRESSION);
		server = serverDbService.save(server);
		
		assertThat(countJobs(), equalTo(2L));
		assertThat(countTriggers(), equalTo(2L));
		serverDbService.delete(server);
		
		assertThat(countJobs(), equalTo(2L));
		assertThat(countTriggers(), equalTo(0L));
	}

}
