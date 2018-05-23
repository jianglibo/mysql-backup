package com.go2wheel.mysqlbackup.repository;


import static com.go2wheel.mysqlbackup.jooqschema.tables.UpTime.UP_TIME;

import java.sql.Timestamp;
import java.util.List;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.UpTimeRecord;
import com.go2wheel.mysqlbackup.model.UpTime;
import com.go2wheel.mysqlbackup.util.SQLTimeUtil;

@Repository
public class JOOQUpTimeRepository extends RepositoryBaseImpl<UpTimeRecord, UpTime> implements UpTimeRepository {

	@Autowired
	protected JOOQUpTimeRepository(DSLContext jooq) {
		super(UP_TIME, UpTime.class, jooq);
	}

	@Override
	public List<UpTime> getItemsInDays(Integer serverId, int days) {
		Timestamp ts = SQLTimeUtil.recentDaysStartPoint(days);
		return jooq.selectFrom(UP_TIME).where(UP_TIME.CREATED_AT.greaterThan(ts)).and(UP_TIME.SERVER_ID.eq(serverId))
				.orderBy(UP_TIME.CREATED_AT.asc()).fetch().into(UpTime.class);
	}
}
