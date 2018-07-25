package com.go2wheel.mysqlbackup.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.SoftwareRecord;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.Software;
import com.go2wheel.mysqlbackup.repository.SoftwareRepository;

@Service
@Validated
public class SoftwareDbService extends DbServiceBase<SoftwareRecord, Software> {
	

	public SoftwareDbService(SoftwareRepository repo) {
		super(repo);
	}

	public Software findByUniqueField(Software software) {
		return  ((SoftwareRepository)repo).findByUniqueField(software);
	}

	public List<Software> findByName(String name) {
		return  ((SoftwareRepository)repo).findByName(name);
	}

	public List<Software> findByServer(Server server) {
		return  ((SoftwareRepository)repo).findByServer(server);
	}

}
