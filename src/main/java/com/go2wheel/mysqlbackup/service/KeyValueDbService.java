package com.go2wheel.mysqlbackup.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.KeyValueRecord;
import com.go2wheel.mysqlbackup.model.KeyValue;
import com.go2wheel.mysqlbackup.repository.KeyValueRepository;

@Service
@Validated
public class KeyValueDbService extends DbServiceBase<KeyValueRecord, KeyValue> {

	public KeyValueDbService(KeyValueRepository repo) {
		super(repo);
	}

	public KeyValue findOneByKey(String key) {
		return ((KeyValueRepository)repo).findOneByKey(key);
	}

	public KeyValue findOneByGroupAndKey(String prefix, String subKey) {
		String key = prefix + "." + subKey;
		return findOneByKey(key);
	}

	public List<KeyValue> findByKeyPrefix(String... keys) {
		String key = String.join(".", keys);
		return ((KeyValueRepository)repo).findByKeyPrefix(key); 
	}
	
}
