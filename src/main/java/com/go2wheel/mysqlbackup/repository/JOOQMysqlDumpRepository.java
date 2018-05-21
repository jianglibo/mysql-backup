package com.go2wheel.mysqlbackup.repository;


import static com.go2wheel.mysqlbackup.jooqschema.tables.MysqlDump.MYSQL_DUMP;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.MysqlDumpRecord;
import com.go2wheel.mysqlbackup.model.MysqlDump;

@Repository
public class JOOQMysqlDumpRepository extends RepositoryBaseImpl<MysqlDumpRecord, MysqlDump> implements MysqlDumpRepository {

	@Autowired
	protected JOOQMysqlDumpRepository(DSLContext jooq) {
		super(MYSQL_DUMP, MysqlDump.class, jooq);
	}
}
