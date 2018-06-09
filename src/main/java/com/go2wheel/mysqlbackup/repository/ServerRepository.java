package com.go2wheel.mysqlbackup.repository;

import java.util.List;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.ServerRecord;
import com.go2wheel.mysqlbackup.model.Server;

public interface ServerRepository extends RepositoryBase<ServerRecord, Server>{

	Server findByHost(String host);

	List<Server> findLikeHost(String partOfHostName);

	List<String> findDistinctOsType(String input);
}
