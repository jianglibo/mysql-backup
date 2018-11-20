package com.go2wheel.mysqlbackup.dbservice;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.SoftwareInstallationRecord;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.Software;
import com.go2wheel.mysqlbackup.model.SoftwareInstallation;
import com.go2wheel.mysqlbackup.repository.SoftwareInstallationRepository;

@Service
@Validated
public class SoftwareInstallationDbService extends DbServiceBase<SoftwareInstallationRecord, SoftwareInstallation> {

	public SoftwareInstallationDbService(SoftwareInstallationRepository repo) {
		super(repo);
	}

	public SoftwareInstallation findByServerAndSoftware(Server server, Software software) {
		return ((SoftwareInstallationRepository)repo).findByServerAndSoftware(server, software);
	}
	
	public SoftwareInstallation findByServerAndSoftware(Integer server, Integer software) {
		return ((SoftwareInstallationRepository)repo).findByServerAndSoftware(server, software);
	}

	
}
