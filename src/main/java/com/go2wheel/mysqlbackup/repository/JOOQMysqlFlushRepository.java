package com.go2wheel.mysqlbackup.repository;


import static com.go2wheel.mysqlbackup.jooqschema.tables.MysqlFlush.MYSQL_FLUSH;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.MysqlFlushRecord;
import com.go2wheel.mysqlbackup.model.MysqlFlush;

@Repository
public class JOOQMysqlFlushRepository extends RepositoryBaseImpl<MysqlFlushRecord, MysqlFlush> implements MysqlFlushRepository {

	@Autowired
	protected JOOQMysqlFlushRepository(DSLContext jooq) {
		super(MYSQL_FLUSH, MysqlFlush.class, jooq);
	}
}
