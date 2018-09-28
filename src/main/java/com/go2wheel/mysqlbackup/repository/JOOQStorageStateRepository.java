package com.go2wheel.mysqlbackup.repository;


import static com.go2wheel.mysqlbackup.jooqschema.tables.StorageState.STORAGE_STATE;

import java.sql.Timestamp;
import java.util.List;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.StorageStateRecord;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.StorageState;
import com.go2wheel.mysqlbackup.util.SQLTimeUtil;

@Repository
public class JOOQStorageStateRepository extends RepositoryBaseImpl<StorageStateRecord, StorageState> implements StorageStateRepository {

	@Autowired
	protected JOOQStorageStateRepository(DSLContext jooq) {
		super(STORAGE_STATE, StorageState.class, jooq);
	}

	@Override
	public List<StorageState> getItemsInDays(Integer serverId, int days) {
		Timestamp ts = SQLTimeUtil.recentDaysStartPoint(days);
		return jooq.selectFrom(STORAGE_STATE).where(STORAGE_STATE.CREATED_AT.greaterThan(ts)).and(STORAGE_STATE.SERVER_ID.eq(serverId))
				.orderBy(STORAGE_STATE.CREATED_AT.asc()).fetch().into(StorageState.class);
	}

	@Override
	public int removeBeforeDay(Server server, int keeyDays) {
		Timestamp ts = SQLTimeUtil.recentDaysStartPoint(keeyDays);
		if (server != null) {
			return jooq.deleteFrom(STORAGE_STATE).where(STORAGE_STATE.CREATED_AT.lessThan(ts).and(STORAGE_STATE.SERVER_ID.eq(server.getId()))).execute();
		} else {
			return jooq.deleteFrom(STORAGE_STATE).where(STORAGE_STATE.CREATED_AT.lessThan(ts)).execute();			
		}
		
	}

	@Override
	public List<StorageState> findByServerId(Integer id) {
		return jooq.selectFrom(STORAGE_STATE).where(STORAGE_STATE.SERVER_ID.eq(id))
				.orderBy(STORAGE_STATE.CREATED_AT.desc()).fetch().into(StorageState.class);
	}

	@Override
	public int deleteBefore(int days) {
		Timestamp ts = SQLTimeUtil.recentDaysStartPoint(days);
		return jooq.deleteFrom(STORAGE_STATE).where(STORAGE_STATE.CREATED_AT.lessThan(ts)).execute();
	}
}
