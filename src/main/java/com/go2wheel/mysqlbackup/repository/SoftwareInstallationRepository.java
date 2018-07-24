package com.go2wheel.mysqlbackup.repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.SoftwareInstallationRecord;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.Software;
import com.go2wheel.mysqlbackup.model.SoftwareInstallation;

public interface SoftwareInstallationRepository extends RepositoryBase<SoftwareInstallationRecord, SoftwareInstallation>{

	SoftwareInstallation findByServerAndSoftware(Server server, Software software);
	SoftwareInstallation findByServerAndSoftware(Integer server, Integer software);
}
