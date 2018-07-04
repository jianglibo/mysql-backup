package com.go2wheel.mysqlbackup.repository;


import static com.go2wheel.mysqlbackup.jooqschema.tables.KeyValue.KEY_VALUE;

import java.util.List;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.KeyValueRecord;
import com.go2wheel.mysqlbackup.model.KeyValue;

@Repository
public class JOOQKeyValueRepository extends RepositoryBaseImpl<KeyValueRecord, KeyValue> implements KeyValueRepository {

	@Autowired
	protected JOOQKeyValueRepository(DSLContext jooq) {
		super(KEY_VALUE, KeyValue.class, jooq);
	}

	@Override
	public List<KeyValue> findByKeyPrefix(String key) {
		return jooq.selectFrom(KEY_VALUE).where(KEY_VALUE.ITEM_KEY.startsWith(key)).fetchInto(KeyValue.class);
	}

}
