package com.go2wheel.mysqlbackup.repository;

import java.util.List;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.DiskfreeRecord;
import com.go2wheel.mysqlbackup.model.Diskfree;

public interface DiskfreeRepository extends RepositoryBase<DiskfreeRecord, Diskfree, Integer>{

	List<Diskfree> getItemsInDays(int serverId, int days);

}
