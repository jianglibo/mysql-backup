package com.go2wheel.mysqlbackup.repository;


import static com.go2wheel.mysqlbackup.jooqschema.tables.KeyKeyValue.KEY_KEY_VALUE;

import java.util.List;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.KeyKeyValueRecord;
import com.go2wheel.mysqlbackup.model.KeyKeyValue;

@Repository
public class JOOQKeyKeyValueRepository extends RepositoryBaseImpl<KeyKeyValueRecord, KeyKeyValue> implements KeyKeyValueRepository {

	@Autowired
	protected JOOQKeyKeyValueRepository(DSLContext jooq) {
		super(KEY_KEY_VALUE, KeyKeyValue.class, jooq);
	}

	@Override
	public List<KeyKeyValue> findByGroupKey(String groupKey) {
		return jooq.selectFrom(KEY_KEY_VALUE).where(KEY_KEY_VALUE.GROUP_KEY.eq(groupKey)).fetchInto(KeyKeyValue.class);
	}

}
