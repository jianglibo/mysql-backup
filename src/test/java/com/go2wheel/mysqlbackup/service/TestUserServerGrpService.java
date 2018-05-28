package com.go2wheel.mysqlbackup.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

import com.go2wheel.mysqlbackup.model.ServerGrp;
import com.go2wheel.mysqlbackup.model.UserAccount;
import com.go2wheel.mysqlbackup.model.UserServerGrp;

public class TestUserServerGrpService extends ServiceTbase {

	@Test
	public void tCreate() {
		UserAccount ua = createAUser();
		ServerGrp sg = createAServerGrp();
		UserServerGrp usg = new UserServerGrp.UserServerGrpBuilder(ua.getId(), sg.getId()).withCronExpression("zbc")
				.build();
		usg = userServerGrpService.save(usg);
		assertThat(usg.getId(), greaterThan(99));
	}
	
	@Test
	public void tFindByUser() {
		UserAccount ua = createAUser();
		ServerGrp sg = createAServerGrp();
		UserServerGrp usg = new UserServerGrp.UserServerGrpBuilder(ua.getId(), sg.getId()).withCronExpression("zbc")
				.build();
		usg = userServerGrpService.save(usg);
		
		List<UserServerGrp> usgs = userServerGrpService.findByUser(ua);
		assertThat(usgs.size(), equalTo(1));

	}

}
