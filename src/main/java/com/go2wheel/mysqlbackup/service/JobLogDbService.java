package com.go2wheel.mysqlbackup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.JobLogRecord;
import com.go2wheel.mysqlbackup.model.JobLog;
import com.go2wheel.mysqlbackup.repository.JobLogRepository;

@Service
@Validated
public class JobLogDbService extends ServiceBase<JobLogRecord, JobLog> {
	
	@Autowired
	private ServerDbService serverDbService;

	public JobLogDbService(JobLogRepository repo) {
		super(repo);
	}
	
	
}
