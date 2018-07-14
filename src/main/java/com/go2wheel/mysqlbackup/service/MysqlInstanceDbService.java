package com.go2wheel.mysqlbackup.service;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.MysqlInstanceRecord;
import com.go2wheel.mysqlbackup.model.MysqlInstance;
import com.go2wheel.mysqlbackup.repository.MysqlInstanceRepository;

@Service
@Validated
public class MysqlInstanceDbService extends DbServiceBase<MysqlInstanceRecord, MysqlInstance> {
	
	public MysqlInstanceDbService(MysqlInstanceRepository repo) {
		super(repo);
	}

	public MysqlInstance findByServerId(Integer id) {
		return ((MysqlInstanceRepository)repo).findByServerId(id);
	}

	public MysqlInstance findByServerId(String serverId) {
		return findByServerId(Integer.parseInt(serverId));
	}
}
