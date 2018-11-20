package com.go2wheel.mysqlbackup.dbservice;

import java.util.List;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.go2wheel.mysqlbackup.model.KeyValue;
import com.go2wheel.mysqlbackup.value.KeyValueProperties;

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

	public List<KeyValue> findByKeyPrefix(String... keys) {
		return keyValueDbService.findByKeyPrefix(keys);
	}

	public KeyValueProperties getPropertiesByPrefix(String... keys) {
		String prefix = String.join(".", keys);
		return new KeyValueProperties(findByKeyPrefix(prefix), prefix);
	}

	public KeyValue save(KeyValue keyValue) {
		return keyValueDbService.save(keyValue);
		
	}

}
