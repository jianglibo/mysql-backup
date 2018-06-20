package com.go2wheel.mysqlbackup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.BackupFolderRecord;
import com.go2wheel.mysqlbackup.model.BackupFolder;
import com.go2wheel.mysqlbackup.repository.BackupFolderRepository;

@Service
@Validated
public class BackupFolderDbService extends DbServiceBase<BackupFolderRecord, BackupFolder> {

	@Autowired
	public BackupFolderDbService(BackupFolderRepository repo) {
		super(repo);
	}

	public BackupFolder findByServerHostAndFolder(String serverHost, String folder) {
		return ((BackupFolderRepository)repo).findByServerAndFolder(serverHost, folder);
	}
}
