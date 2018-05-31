package com.go2wheel.mysqlbackup.service;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.BorgDescriptionRecord;
import com.go2wheel.mysqlbackup.model.BorgDescription;
import com.go2wheel.mysqlbackup.repository.BorgDescriptionRepository;

@Service
@Validated
public class BorgDescriptionService extends ServiceBase<BorgDescriptionRecord, BorgDescription> {
	
	public BorgDescriptionService(BorgDescriptionRepository repo) {
		super(repo);
	}

	public BorgDescription findByServerId(Integer id) {
		return ((BorgDescriptionRepository)repo).findByServerId(id);
	}
}
