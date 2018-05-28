package com.go2wheel.mysqlbackup.repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.BackupFolderRecord;
import com.go2wheel.mysqlbackup.model.BackupFolder;

public interface BackupFolderRepository extends RepositoryBase<BackupFolderRecord, BackupFolder>{

	BackupFolder findByServerAndFolder(String serverHost, String folder);

}
