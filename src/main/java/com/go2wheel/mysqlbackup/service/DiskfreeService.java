package com.go2wheel.mysqlbackup.service;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.DiskfreeRecord;
import com.go2wheel.mysqlbackup.model.Diskfree;
import com.go2wheel.mysqlbackup.repository.DiskfreeRepository;

@Service
@Validated
public class DiskfreeService extends ServiceBase<DiskfreeRecord, Diskfree> {

	public DiskfreeService(DiskfreeRepository repo) {
		super(repo);
	}
	
}
