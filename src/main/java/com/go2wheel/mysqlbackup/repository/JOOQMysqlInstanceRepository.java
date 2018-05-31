package com.go2wheel.mysqlbackup.repository;


import static com.go2wheel.mysqlbackup.jooqschema.tables.MysqlInstance.MYSQL_INSTANCE;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.MysqlInstanceRecord;
import com.go2wheel.mysqlbackup.model.MysqlInstance;

@Repository
public class JOOQMysqlInstanceRepository extends RepositoryBaseImpl<MysqlInstanceRecord, MysqlInstance> implements MysqlInstanceRepository {

	@Autowired
	protected JOOQMysqlInstanceRepository(DSLContext jooq) {
		super(MYSQL_INSTANCE, MysqlInstance.class, jooq);
	}

	@Override
	public MysqlInstance findByServerId(Integer id) {
		return jooq.selectFrom(MYSQL_INSTANCE).where(MYSQL_INSTANCE.SERVER_ID.eq(id)).fetchOneInto(MysqlInstance.class);
	}
}
