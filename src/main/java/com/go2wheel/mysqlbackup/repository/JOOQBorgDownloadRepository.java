package com.go2wheel.mysqlbackup.repository;


import static com.go2wheel.mysqlbackup.jooqschema.tables.BorgDownload.BORG_DOWNLOAD;

import java.sql.Timestamp;
import java.util.List;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.BorgDownloadRecord;
import com.go2wheel.mysqlbackup.model.BorgDownload;
import com.go2wheel.mysqlbackup.util.SQLTimeUtil;

@Repository
public class JOOQBorgDownloadRepository extends RepositoryBaseImpl<BorgDownloadRecord, BorgDownload> implements BorgDownloadRepository {

	@Autowired
	protected JOOQBorgDownloadRepository(DSLContext jooq) {
		super(BORG_DOWNLOAD, BorgDownload.class, jooq);
	}

	@Override
	public List<BorgDownload> getItemsInDays(int serverId, int days) {
		Timestamp ts = SQLTimeUtil.recentDaysStartPoint(days);
		return jooq.selectFrom(BORG_DOWNLOAD).where(BORG_DOWNLOAD.CREATED_AT.greaterThan(ts)).and(BORG_DOWNLOAD.SERVER_ID.eq(serverId)).fetch().into(BorgDownload.class);
	}
}
