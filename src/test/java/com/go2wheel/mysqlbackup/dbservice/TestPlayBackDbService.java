package com.go2wheel.mysqlbackup.dbservice;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Test;
import org.quartz.SchedulerException;

import com.go2wheel.mysqlbackup.model.PlayBack;
import com.go2wheel.mysqlbackup.model.Server;

public class TestPlayBackDbService extends ServiceTbase {

	@Test
	public void tCreate() throws SchedulerException {
		clearDb();
		
		Server server1 = createServer();
		Server server2 = createServer("abc");
		deleteAllJobs();
		
		PlayBack pb = new PlayBack();
		pb.setSourceServerId(server1.getId());
		pb.setTargetServerId(server2.getId());
		pb.setPlayWhat(PlayBack.PLAY_BORG);
		pb.setPairs(Arrays.asList("a=b", "b=d"));
		pb = playBackDbService.save(pb);
		assertThat(pb.getId(), greaterThan(90));
		
		long count = playBackDbService.count();
		
		assertThat(count, equalTo(1L));

	}

}
