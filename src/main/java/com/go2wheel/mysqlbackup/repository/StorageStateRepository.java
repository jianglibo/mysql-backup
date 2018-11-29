package com.go2wheel.mysqlbackup.repository;

import java.util.List;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.StorageStateRecord;
import com.go2wheel.mysqlbackup.model.StorageState;
import com.go2wheel.mysqlbackup.value.Server;

public interface StorageStateRepository extends RepositoryBase<StorageStateRecord, StorageState>{

	List<StorageState> getItemsInDays(Integer serverId, int days);

	int removeBeforeDay(Server server, int keeyDays);

	List<StorageState> findByServerId(Integer id);

	int deleteBefore(int days);

}
