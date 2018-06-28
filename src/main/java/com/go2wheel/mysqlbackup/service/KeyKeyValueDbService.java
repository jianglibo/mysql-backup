package com.go2wheel.mysqlbackup.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.KeyKeyValueRecord;
import com.go2wheel.mysqlbackup.model.KeyKeyValue;
import com.go2wheel.mysqlbackup.repository.KeyKeyValueRepository;
import com.go2wheel.mysqlbackup.value.KkvWrapper;

@Service
@Validated
public class KeyKeyValueDbService extends DbServiceBase<KeyKeyValueRecord, KeyKeyValue> {
	
	public KeyKeyValueDbService(KeyKeyValueRepository repo) {
		super(repo);
	}
	
	public KkvWrapper getGroup(String groupKey) {
		List<KeyKeyValue> kkvs = ((KeyKeyValueRepository)repo).findByGroupKey(groupKey);
		return new KkvWrapper(kkvs.stream().collect(Collectors.toMap(kkv -> kkv.getItemKey(), kkv -> kkv.getItemValue())));
	}

}
