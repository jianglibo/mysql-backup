package com.go2wheel.mysqlbackup.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.JobErrorRecord;
import com.go2wheel.mysqlbackup.model.JobError;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.repository.JobErrorRepository;

@Service
@Validated
public class JobErrorDbService extends ServiceBase<JobErrorRecord, JobError> {
	

	public JobErrorDbService(JobErrorRepository repo) {
		super(repo);
	}

	public List<JobError> getItemsInDays(Server server, int days) {
		return  ((JobErrorRepository)repo).getItemsInDays(server.getId(), days);
	}
	
	
}
