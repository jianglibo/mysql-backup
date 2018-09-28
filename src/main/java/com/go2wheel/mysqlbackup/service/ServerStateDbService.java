package com.go2wheel.mysqlbackup.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.ServerStateRecord;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.ServerState;
import com.go2wheel.mysqlbackup.repository.ServerStateRepository;

@Service
@Validated
public class ServerStateDbService extends DbServiceBase<ServerStateRecord, ServerState> {
	
	public ServerStateDbService(ServerStateRepository repo) {
		super(repo);
	}

	public List<ServerState> getItemsInDays(Server server, int days) {
		return  ((ServerStateRepository)repo).getItemsInDays(server.getId(), days);
	}

	public List<ServerState> findByServerId(Integer id) {
		return  ((ServerStateRepository)repo).findByServerId(id);
	}

	public int deleteBefore(int days) {
		return  ((ServerStateRepository)repo).deleteBefore(days);
	}
	
}
