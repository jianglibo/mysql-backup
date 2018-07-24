package com.go2wheel.mysqlbackup.service;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.JobLogRecord;
import com.go2wheel.mysqlbackup.model.JobLog;
import com.go2wheel.mysqlbackup.repository.JobLogRepository;

@Service
@Validated
public class JobLogDbService extends DbServiceBase<JobLogRecord, JobLog> {
	
	public JobLogDbService(JobLogRepository repo) {
		super(repo);
	}

	
	
}
