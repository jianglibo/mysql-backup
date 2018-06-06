package com.go2wheel.mysqlbackup.repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.BigObRecord;
import com.go2wheel.mysqlbackup.model.BigOb;

public interface BigObRepository extends RepositoryBase<BigObRecord, BigOb>{

	BigOb findByName(String name);

}
