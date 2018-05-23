package com.go2wheel.mysqlbackup.repository;

import java.util.List;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.UpTimeRecord;
import com.go2wheel.mysqlbackup.model.UpTime;

public interface UpTimeRepository extends RepositoryBase<UpTimeRecord, UpTime, Integer>{

	List<UpTime> getItemsInDays(Integer serverId, int days);

}
