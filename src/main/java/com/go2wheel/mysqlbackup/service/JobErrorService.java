package com.go2wheel.mysqlbackup.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.JobErrorRecord;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.JobError;
import com.go2wheel.mysqlbackup.repository.JobErrorRepository;
import com.go2wheel.mysqlbackup.value.Box;

@Service
@Validated
public class JobErrorService extends ServiceBase<JobErrorRecord, JobError> {
	
	@Autowired
	private ServerService serverService;

	public JobErrorService(JobErrorRepository repo) {
		super(repo);
	}

	public List<JobError> getItemsInDays(Box box, int days) {
		Server server = serverService.findByHost(box.getHost());
		return  ((JobErrorRepository)repo).getItemsInDays(server.getId(), days);
	}
	
	
}
