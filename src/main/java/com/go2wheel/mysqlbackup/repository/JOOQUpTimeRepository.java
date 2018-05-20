package com.go2wheel.mysqlbackup.repository;


import static com.go2wheel.mysqlbackup.jooqschema.tables.UpTime.UP_TIME;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.UpTimeRecord;
import com.go2wheel.mysqlbackup.model.UpTime;

@Repository
public class JOOQUpTimeRepository extends RepositoryBaseImpl<UpTimeRecord, UpTime> implements UpTimeRepository {

	@Autowired
	protected JOOQUpTimeRepository(DSLContext jooq) {
		super(UP_TIME, UpTime.class, jooq);
	}
}
