package com.go2wheel.mysqlbackup.service;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.MysqlDumpRecord;
import com.go2wheel.mysqlbackup.model.MysqlDump;
import com.go2wheel.mysqlbackup.repository.MysqlDumpRepository;

@Service
@Validated
public class MysqlDumpService extends ServiceBase<MysqlDumpRecord, MysqlDump> {

	public MysqlDumpService(MysqlDumpRepository repo) {
		super(repo);
	}
	
}
