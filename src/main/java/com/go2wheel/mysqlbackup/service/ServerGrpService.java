package com.go2wheel.mysqlbackup.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.ServerGrpRecord;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.ServerGrp;
import com.go2wheel.mysqlbackup.repository.ServerGrpRepository;

@Service
@Validated
public class ServerGrpService extends ServiceBase<ServerGrpRecord, ServerGrp> {

	@Autowired
	public ServerGrpService(ServerGrpRepository serverGrpRepository) {
		super(serverGrpRepository);
	}
	
	public void addServer(ServerGrp serverGrp, Server server) {
		((ServerGrpRepository)repo).addServer(serverGrp, server);
	}
	
	public void removeServer(ServerGrp serverGrp, Server server) {
		((ServerGrpRepository)repo).removeServer(serverGrp, server);
	}
	
	public void removeServer(ServerGrp serverGrp, int serverId) {
		((ServerGrpRepository)repo).removeServer(serverGrp, serverId);
	}



	public List<Server> getServers(ServerGrp serverGrp) {
		return ((ServerGrpRepository)repo).getServers(serverGrp);
	}

	public List<ServerGrp> findLikeEname(String input) {
		return ((ServerGrpRepository)repo).findLikeEname(input);
	}

	public ServerGrp findByEname(String ename) {
		return ((ServerGrpRepository)repo).findByEname(ename);
	}
	
}
