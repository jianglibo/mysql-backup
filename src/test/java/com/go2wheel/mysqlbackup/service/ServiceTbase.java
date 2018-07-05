package com.go2wheel.mysqlbackup.service;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.model.ServerGrp;
import com.go2wheel.mysqlbackup.model.UserAccount;

public class ServiceTbase extends SpringBaseFort {

	protected UserAccount createAUser() {
		UserAccount ua = new UserAccount.UserAccountBuilder("ab", "a@b.c").build();
		return userAccountDbService.save(ua);
	}
	
	protected ServerGrp createAServerGrp() {
		return createAServerGrp("abc1");
	}
	
	protected ServerGrp createAServerGrp(String gname) {
		ServerGrp serverGrp = new ServerGrp(gname);
		return serverGrpDbService.save(serverGrp);
	}
	
}
