package com.go2wheel.mysqlbackup.repository;

import java.util.List;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.KeyKeyValueRecord;
import com.go2wheel.mysqlbackup.model.KeyKeyValue;

public interface KeyKeyValueRepository extends RepositoryBase<KeyKeyValueRecord, KeyKeyValue> {

	List<KeyKeyValue> findByGroupKey(String groupKey);

}
