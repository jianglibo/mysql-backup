package com.go2wheel.mysqlbackup.service;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.BorgDownloadRecord;
import com.go2wheel.mysqlbackup.model.BorgDownload;
import com.go2wheel.mysqlbackup.repository.BorgDownloadRepository;

@Service
@Validated
public class BorgDownloadService extends ServiceBase<BorgDownloadRecord, BorgDownload> {

	public BorgDownloadService(BorgDownloadRepository repo) {
		super(repo);
	}
	
}
