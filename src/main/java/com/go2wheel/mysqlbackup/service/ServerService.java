package com.go2wheel.mysqlbackup.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.ServerRecord;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.repository.ServerRepository;

@Service
@Validated
public class ServerService extends ServiceBase<ServerRecord, Server> {

	@Autowired
	public ServerService(ServerRepository serverRepository) {
		super(serverRepository);
	}
	
	public Server findByHost(String host) {
		return ((ServerRepository)repo).findByHost(host);
	}

	public List<Server> findLikeHost(String partOfHostName) {
		return ((ServerRepository)repo).findLikeHost(partOfHostName);
	}
}
