package com.go2wheel.mysqlbackup.service;

import java.util.List;

import org.jooq.UpdatableRecord;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.model.BaseModel;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.ServerGrp;
import com.go2wheel.mysqlbackup.model.UserAccount;

public class ServiceTbase extends SpringBaseFort {
	
	protected String serverHost = "192.168.33.110";
	
	public ServiceTbase() {
		super(false);
	}
	
	protected <R extends UpdatableRecord<R>,M extends BaseModel, S extends ServiceBase<R, M>>  void deleteAll(S service) {
		List<M> ms = service.findAll(); 
		ms.stream().forEach(pojo -> service.delete(pojo));
	}

	protected UserAccount createAUser() {
		UserAccount ua = new UserAccount.UserAccountBuilder("ab", "a@b.c").build();
		return userAccountService.save(ua);
	}
	
	protected ServerGrp createAServerGrp() {
		return createAServerGrp("abc1");
	}
	
	protected ServerGrp createAServerGrp(String gname) {
		ServerGrp serverGrp = new ServerGrp(gname);
		return serverGrpService.save(serverGrp);
	}
	
	protected Server createAServer() {
		Server s = serverService.findByHost(serverHost);
		if (s != null) {
			return s;
		}
		s = new Server(serverHost);
		return serverService.save(s);
	}
}
