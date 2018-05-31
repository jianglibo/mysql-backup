package com.go2wheel.mysqlbackup.repository;


import static com.go2wheel.mysqlbackup.jooqschema.tables.KeyValue.KEY_VALUE;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.KeyValueRecord;
import com.go2wheel.mysqlbackup.model.KeyValueInDb;

@Repository
public class JOOQKeyValueInDbRepository extends RepositoryBaseImpl<KeyValueRecord, KeyValueInDb> implements KeyValueInDbRepository {

	@Autowired
	protected JOOQKeyValueInDbRepository(DSLContext jooq) {
		super(KEY_VALUE, KeyValueInDb.class, jooq);
	}
}
