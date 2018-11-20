package com.go2wheel.mysqlbackup.dbservice;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.BigObRecord;
import com.go2wheel.mysqlbackup.model.BigOb;
import com.go2wheel.mysqlbackup.repository.BigObRepository;

@Service
@Validated
public class BigObDbService extends DbServiceBase<BigObRecord, BigOb> {
	
	public BigObDbService(BigObRepository repo) {
		super(repo);
	}

	public BigOb findByName(String name) {
		return ((BigObRepository)repo).findByName(name);
	}
}
