package com.go2wheel.mysqlbackup.dbservice;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

import com.go2wheel.mysqlbackup.model.ServerGrp;
import com.go2wheel.mysqlbackup.model.UserAccount;
import com.go2wheel.mysqlbackup.model.Subscribe;

public class TestUserServerGrpService extends ServiceTbase {

	@Test
	public void tCreate() {
		UserAccount ua = createAUser();
		ServerGrp sg = createAServerGrp();
		Subscribe usg = new Subscribe.SubscribeBuilder(ua.getId(), sg.getId() , A_VALID_CRON_EXPRESSION, "amnese")
				.build();
		usg = subscribeDbService.save(usg);
		assertThat(usg.getId(), greaterThan(99));
	}
	
	@Test
	public void tFindByUser() {
		UserAccount ua = createAUser();
		ServerGrp sg = createAServerGrp();
		Subscribe usg = new Subscribe.SubscribeBuilder(ua.getId(), sg.getId(), A_VALID_CRON_EXPRESSION, "amnese")
				.build();
		usg = subscribeDbService.save(usg);
		
		ServerGrp sg1 = createAServerGrp("kku");
		usg = new Subscribe.SubscribeBuilder(ua.getId(), sg1.getId(), A_VALID_CRON_EXPRESSION, "amenese")
				.build();
		usg = subscribeDbService.save(usg);
		
		List<Subscribe> usgs = subscribeDbService.findByUser(ua);
		assertThat(usgs.size(), equalTo(2));

	}

}
