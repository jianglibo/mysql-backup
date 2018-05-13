package com.go2wheel.mysqlbackup.repository;


import static com.go2wheel.mysqlbackup.jooqschema.tables.ReuseableCron.REUSEABLE_CRON;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.ReuseableCronRecord;
import com.go2wheel.mysqlbackup.model.ReusableCron;

@Repository
public class JOOQReusableCronRepository extends RepositoryBaseImpl<ReuseableCronRecord, ReusableCron> implements ReusableCronRepository {

	@Autowired
	protected JOOQReusableCronRepository(DSLContext jooq) {
		super(REUSEABLE_CRON, ReusableCron.class, jooq);
	}
}
