package com.go2wheel.mysqlbackup.repository;


import static com.go2wheel.mysqlbackup.jooqschema.tables.BorgDownload.BORG_DOWNLOAD;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.BorgDownloadRecord;
import com.go2wheel.mysqlbackup.model.BorgDownload;

@Repository
public class JOOQBorgDownloadRepository extends RepositoryBaseImpl<BorgDownloadRecord, BorgDownload> implements BorgDownloadRepository {

	@Autowired
	protected JOOQBorgDownloadRepository(DSLContext jooq) {
		super(BORG_DOWNLOAD, BorgDownload.class, jooq);
	}
}
