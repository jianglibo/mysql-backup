package com.go2wheel.mysqlbackup.service;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.SoftwareRecord;
import com.go2wheel.mysqlbackup.model.Software;
import com.go2wheel.mysqlbackup.repository.SoftwareRepository;

@Service
@Validated
public class SoftwareDbService extends DbServiceBase<SoftwareRecord, Software> {

	public SoftwareDbService(SoftwareRepository repo) {
		super(repo);
	}
}
