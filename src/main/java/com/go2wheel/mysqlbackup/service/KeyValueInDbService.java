package com.go2wheel.mysqlbackup.service;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.KeyValueRecord;
import com.go2wheel.mysqlbackup.model.KeyValueInDb;
import com.go2wheel.mysqlbackup.repository.KeyValueInDbRepository;

@Service
@Validated
public class KeyValueInDbService extends ServiceBase<KeyValueRecord, KeyValueInDb> {
	
	public KeyValueInDbService(KeyValueInDbRepository repo) {
		super(repo);
	}
}
