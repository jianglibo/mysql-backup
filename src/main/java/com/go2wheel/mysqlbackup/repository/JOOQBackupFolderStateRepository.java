package com.go2wheel.mysqlbackup.repository;


import static com.go2wheel.mysqlbackup.jooqschema.tables.BackupFolderState.BACKUP_FOLDER_STATE;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.BackupFolderStateRecord;
import com.go2wheel.mysqlbackup.model.BackupFolderState;

@Repository
public class JOOQBackupFolderStateRepository extends RepositoryBaseImpl<BackupFolderStateRecord, BackupFolderState> implements BackupFolderStateRepository {
	@Autowired
	protected JOOQBackupFolderStateRepository(DSLContext jooq) {
		super(BACKUP_FOLDER_STATE, BackupFolderState.class, jooq);
	}

}
