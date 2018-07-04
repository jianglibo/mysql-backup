package com.go2wheel.mysqlbackup.service;

import java.util.List;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.go2wheel.mysqlbackup.model.KeyValue;

@Service
public class KeyValueService {

	@Autowired
	private KeyValueDbService keyValueDbService;
	
	@Autowired
	private DSLContext jooq;

	public KeyValue create(String key, String value) {
		KeyValue kv = new KeyValue(key, value);
		return keyValueDbService.save(kv);
	}

	public List<KeyValue> findByKeyPrefix(String key) {
		return keyValueDbService.findByKeyPrefix(key);
	}

}
