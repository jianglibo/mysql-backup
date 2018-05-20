package com.go2wheel.mysqlbackup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.BackupFolderStateRecord;
import com.go2wheel.mysqlbackup.model.BackupFolderState;
import com.go2wheel.mysqlbackup.repository.BackupFolderStateRepository;

@Service
@Validated
public class BackupFolderStateService extends ServiceBase<BackupFolderStateRecord, BackupFolderState> {

	@Autowired
	public BackupFolderStateService(BackupFolderStateRepository repo) {
		super(repo);
	}
}
