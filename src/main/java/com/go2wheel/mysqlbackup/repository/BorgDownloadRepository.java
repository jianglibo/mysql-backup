package com.go2wheel.mysqlbackup.repository;

import java.util.List;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.BorgDownloadRecord;
import com.go2wheel.mysqlbackup.model.BorgDownload;

public interface BorgDownloadRepository extends RepositoryBase<BorgDownloadRecord, BorgDownload>{

	List<BorgDownload> getItemsInDays(int serverId, int days);

}
