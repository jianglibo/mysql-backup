package com.go2wheel.mysqlbackup.service;


import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.go2wheel.mysqlbackup.model.UserAccount;

public class TestUserAccountService extends ServiceTbase {
	
	@Test
	public void tCreate() {
		UserAccount ua = new UserAccount.UserAccountBuilder("ab", "a@b.c").build();
		ua = userAccountService.save(ua);
		assertNotNull(ua.getMobile());
		assertNotNull(ua.getCreatedAt());
		assertThat(ua.getId(), greaterThan(99));
	}

}
