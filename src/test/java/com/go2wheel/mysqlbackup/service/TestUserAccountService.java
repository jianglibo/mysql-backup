package com.go2wheel.mysqlbackup.service;


import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

import com.go2wheel.mysqlbackup.model.UserAccount;

public class TestUserAccountService extends ServiceTbase {
	
	@Test
	public void tCreate() throws InterruptedException {
		UserAccount ua = new UserAccount.UserAccountBuilder("ab", "a@b.c").build();
		ua = userAccountService.save(ua);
		Thread.sleep(1000);
		UserAccount ua1 = new UserAccount.UserAccountBuilder("ab1", "a1@b.c").build();
		ua1 = userAccountService.save(ua1);
		assertNotNull(ua.getMobile());
		assertNotNull(ua.getCreatedAt());
		assertThat(ua.getId(), greaterThan(99));
		
		List<UserAccount> uas = userAccountService.findAll(0, 2);
		assertThat(uas.size(), equalTo(2));

		uas = userAccountService.findAll(0, 20);
		assertThat(uas.size(), equalTo(2));
		
		uas = userAccountService.findAll(com.go2wheel.mysqlbackup.jooqschema.tables.UserAccount.USER_ACCOUNT.CREATED_AT.desc(), 0, 2);
		assertThat(uas.get(0).getId(), equalTo(ua1.getId()));
		assertThat(uas.get(1).getId(), equalTo(ua.getId()));
		
		uas = userAccountService.findAll(com.go2wheel.mysqlbackup.jooqschema.tables.UserAccount.USER_ACCOUNT.CREATED_AT.asc(), 0, 2);
		assertThat(uas.get(0).getId(), equalTo(ua.getId()));
		assertThat(uas.get(1).getId(), equalTo(ua1.getId()));
	}

}
