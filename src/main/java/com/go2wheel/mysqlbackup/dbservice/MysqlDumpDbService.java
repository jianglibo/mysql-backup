package com.go2wheel.mysqlbackup.dbservice;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.MysqlDumpRecord;
import com.go2wheel.mysqlbackup.model.MysqlDump;
import com.go2wheel.mysqlbackup.repository.MysqlDumpRepository;

@Service
@Validated
public class MysqlDumpDbService extends DbServiceBase<MysqlDumpRecord, MysqlDump> {

	public MysqlDumpDbService(MysqlDumpRepository repo) {
		super(repo);
	}
	
}
