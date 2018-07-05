package com.go2wheel.mysqlbackup.repository;

import java.util.List;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.KeyValueRecord;
import com.go2wheel.mysqlbackup.model.KeyValue;

public interface KeyValueRepository extends RepositoryBase<KeyValueRecord, KeyValue>{

	List<KeyValue> findByKeyPrefix(String key);

	KeyValue findOneByKey(String key);

}
