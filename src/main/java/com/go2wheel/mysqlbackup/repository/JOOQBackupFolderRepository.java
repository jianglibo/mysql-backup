package com.go2wheel.mysqlbackup.repository;

import static com.go2wheel.mysqlbackup.jooqschema.tables.BackupFolder.BACKUP_FOLDER;
import static com.go2wheel.mysqlbackup.jooqschema.tables.Server.SERVER;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.BackupFolderRecord;
import com.go2wheel.mysqlbackup.model.BackupFolder;

@Repository
public class JOOQBackupFolderRepository extends RepositoryBaseImpl<BackupFolderRecord, BackupFolder>
		implements BackupFolderRepository {

	@Autowired
	protected JOOQBackupFolderRepository(DSLContext jooq) {
		super(BACKUP_FOLDER, BackupFolder.class, jooq);
	}

	@Override
	public BackupFolder findByServerAndFolder(String serverHost, String folder) {
		Record	bfr = jooq.select(BACKUP_FOLDER.fields()).from(BACKUP_FOLDER).join(SERVER).on(BACKUP_FOLDER.SERVER_ID.eq(SERVER.ID))
				.where(SERVER.HOST.eq(serverHost)).and(BACKUP_FOLDER.FOLDER.eq(folder)).fetchOne();
		 
		if (bfr != null) {
			 return bfr.into(BackupFolder.class);	
		} else {
			return null;
		}
		
	}
}
