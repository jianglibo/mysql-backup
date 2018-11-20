package com.go2wheel.mysqlbackup.dbservice;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.RobocopyDescriptionRecord;
import com.go2wheel.mysqlbackup.model.RobocopyDescription;
import com.go2wheel.mysqlbackup.repository.RobocopyDescriptionRepository;

@Service
@Validated
public class RobocopyDescriptionDbService extends DbServiceBase<RobocopyDescriptionRecord, RobocopyDescription> {
	
	public RobocopyDescriptionDbService(RobocopyDescriptionRepository repo) {
		super(repo);
	}

	public RobocopyDescription findByServerId(Integer id) {
		return ((RobocopyDescriptionRepository)repo).findByServerId(id);
	}

	public RobocopyDescription findByServerId(String serverId) {
		return findByServerId(Integer.parseInt(serverId));
	}
}
