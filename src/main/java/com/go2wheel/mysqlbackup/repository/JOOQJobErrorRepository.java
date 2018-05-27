package com.go2wheel.mysqlbackup.repository;


import static com.go2wheel.mysqlbackup.jooqschema.tables.JobError.JOB_ERROR;

import java.sql.Timestamp;
import java.util.List;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.JobErrorRecord;
import com.go2wheel.mysqlbackup.model.JobError;
import com.go2wheel.mysqlbackup.util.SQLTimeUtil;

@Repository
public class JOOQJobErrorRepository extends RepositoryBaseImpl<JobErrorRecord, JobError> implements JobErrorRepository {

	@Autowired
	protected JOOQJobErrorRepository(DSLContext jooq) {
		super(JOB_ERROR, JobError.class, jooq);
	}

	@Override
	public List<JobError> getItemsInDays(Integer serverId, int days) {
		Timestamp ts = SQLTimeUtil.recentDaysStartPoint(days);
		return jooq.selectFrom(JOB_ERROR).where(JOB_ERROR.CREATED_AT.greaterThan(ts)).and(JOB_ERROR.SERVER_ID.eq(serverId))
				.orderBy(JOB_ERROR.CREATED_AT.asc()).fetch().into(JobError.class);
	}
}
