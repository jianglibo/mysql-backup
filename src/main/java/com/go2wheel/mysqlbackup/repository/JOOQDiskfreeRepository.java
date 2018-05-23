package com.go2wheel.mysqlbackup.repository;

import static com.go2wheel.mysqlbackup.jooqschema.tables.Diskfree.DISKFREE;

import java.sql.Timestamp;
import java.util.List;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.DiskfreeRecord;
import com.go2wheel.mysqlbackup.model.Diskfree;
import com.go2wheel.mysqlbackup.util.SQLTimeUtil;

@Repository
public class JOOQDiskfreeRepository extends RepositoryBaseImpl<DiskfreeRecord, Diskfree> implements DiskfreeRepository {

	@Autowired
	protected JOOQDiskfreeRepository(DSLContext jooq) {
		super(DISKFREE, Diskfree.class, jooq);
	}

	@Override
	public List<Diskfree> getItemsInDays(int serverId, int days) {
		Timestamp ts = SQLTimeUtil.recentDaysStartPoint(days);
		return jooq.selectFrom(DISKFREE).where(DISKFREE.CREATED_AT.greaterThan(ts)).and(DISKFREE.SERVER_ID.eq(serverId))
				.orderBy(DISKFREE.CREATED_AT.asc()).fetch().into(Diskfree.class);
	}
}
