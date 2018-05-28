package com.go2wheel.mysqlbackup.repository;

import java.util.List;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.ServerGrpRecord;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.ServerGrp;

public interface ServerGrpRepository extends RepositoryBase<ServerGrpRecord, ServerGrp>{

	void addServer(ServerGrp serverGrp, Server server);

	List<Server> getServers(ServerGrp serverGrp);

	void removeServer(ServerGrp serverGrp, Server server);

	void removeServer(ServerGrp serverGrp, int serverId);

}
