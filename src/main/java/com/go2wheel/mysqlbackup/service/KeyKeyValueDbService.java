package com.go2wheel.mysqlbackup.service;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.KeyKeyValueRecord;
import com.go2wheel.mysqlbackup.model.KeyKeyValue;
import com.go2wheel.mysqlbackup.repository.KeyKeyValueRepository;

@Service
@Validated
public class KeyKeyValueDbService extends DbServiceBase<KeyKeyValueRecord, KeyKeyValue> {
	
	public KeyKeyValueDbService(KeyKeyValueRepository repo) {
		super(repo);
	}

}
