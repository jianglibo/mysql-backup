package com.go2wheel.mysqlbackup.repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.MysqlInstanceRecord;
import com.go2wheel.mysqlbackup.model.MysqlInstance;

public interface MysqlInstanceRepository extends RepositoryBase<MysqlInstanceRecord, MysqlInstance>{

	MysqlInstance findByServerId(Integer id);

}
