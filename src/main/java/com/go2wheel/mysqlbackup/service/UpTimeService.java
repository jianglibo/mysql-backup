package com.go2wheel.mysqlbackup.service;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.UpTimeRecord;
import com.go2wheel.mysqlbackup.model.UpTime;
import com.go2wheel.mysqlbackup.repository.UpTimeRepository;

@Service
@Validated
public class UpTimeService extends ServiceBase<UpTimeRecord, UpTime> {

	public UpTimeService(UpTimeRepository repo) {
		super(repo);
	}
	
}
