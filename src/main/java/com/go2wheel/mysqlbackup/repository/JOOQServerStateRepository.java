package com.go2wheel.mysqlbackup.repository;


import static com.go2wheel.mysqlbackup.jooqschema.tables.ServerState.SERVER_STATE;

import java.sql.Timestamp;
import java.util.List;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.ServerStateRecord;
import com.go2wheel.mysqlbackup.model.ServerState;
import com.go2wheel.mysqlbackup.util.SQLTimeUtil;

@Repository
public class JOOQServerStateRepository extends RepositoryBaseImpl<ServerStateRecord, ServerState> implements ServerStateRepository {

	@Autowired
	protected JOOQServerStateRepository(DSLContext jooq) {
		super(SERVER_STATE, ServerState.class, jooq);
	}

	@Override
	public List<ServerState> getItemsInDays(Integer serverId, int days) {
		Timestamp ts = SQLTimeUtil.recentDaysStartPoint(days);
		return jooq.selectFrom(SERVER_STATE).where(SERVER_STATE.CREATED_AT.greaterThan(ts)).and(SERVER_STATE.SERVER_ID.eq(serverId))
				.orderBy(SERVER_STATE.CREATED_AT.asc()).fetch().into(ServerState.class);
	}

	@Override
	public List<ServerState> findByServerId(Integer id) {
		return jooq.selectFrom(SERVER_STATE).where(SERVER_STATE.SERVER_ID.eq(id))
				.orderBy(SERVER_STATE.CREATED_AT.desc()).fetch().into(ServerState.class);
	}

	@Override
	public int deleteBefore(int days) {
		Timestamp ts = SQLTimeUtil.recentDaysStartPoint(days);
		return jooq.deleteFrom(SERVER_STATE).where(SERVER_STATE.CREATED_AT.lessThan(ts)).execute();
	}
}
