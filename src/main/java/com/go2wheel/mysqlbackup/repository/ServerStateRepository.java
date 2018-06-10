package com.go2wheel.mysqlbackup.repository;

import java.util.List;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.ServerStateRecord;
import com.go2wheel.mysqlbackup.model.ServerState;

public interface ServerStateRepository extends RepositoryBase<ServerStateRecord, ServerState>{

	List<ServerState> getItemsInDays(Integer serverId, int days);

}
