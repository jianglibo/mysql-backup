package com.go2wheel.mysqlbackup.repository;

import java.util.List;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.JobErrorRecord;
import com.go2wheel.mysqlbackup.model.JobError;

public interface JobErrorRepository extends RepositoryBase<JobErrorRecord, JobError>{

	List<JobError> getItemsInDays(Integer serverId, int days);

}
