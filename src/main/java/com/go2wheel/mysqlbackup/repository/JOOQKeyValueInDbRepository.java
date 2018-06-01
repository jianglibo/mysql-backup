package com.go2wheel.mysqlbackup.repository;

import static com.go2wheel.mysqlbackup.jooqschema.tables.KeyValue.KEY_VALUE;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.KeyValueRecord;
import com.go2wheel.mysqlbackup.model.KeyValueInDb;

@Repository
public class JOOQKeyValueInDbRepository extends RepositoryBaseImpl<KeyValueRecord, KeyValueInDb>
		implements KeyValueInDbRepository {

	@Autowired
	protected JOOQKeyValueInDbRepository(DSLContext jooq) {
		super(KEY_VALUE, KeyValueInDb.class, jooq);
	}

	@Override
	public KeyValueInDb findByIdNameKey(int appStateId, String appStateName, String appStateLastServerId) {
		return jooq
				.selectFrom(KEY_VALUE).where(KEY_VALUE.OBJECT_ID.eq(appStateId)
						.and(KEY_VALUE.OBJECT_NAME.eq(appStateName)).and(KEY_VALUE.THE_KEY.eq(appStateLastServerId)))
				.fetchOneInto(KeyValueInDb.class);
	}
}
