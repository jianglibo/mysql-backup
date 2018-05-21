package com.go2wheel.mysqlbackup.repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.MysqlFlushRecord;
import com.go2wheel.mysqlbackup.model.MysqlFlush;

public interface MysqlFlushRepository extends RepositoryBase<MysqlFlushRecord, MysqlFlush, Integer>{

}
