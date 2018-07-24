package com.go2wheel.mysqlbackup.repository;


import static com.go2wheel.mysqlbackup.jooqschema.tables.SoftwareInstallation.SOFTWARE_INSTALLATION;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.SoftwareInstallationRecord;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.Software;
import com.go2wheel.mysqlbackup.model.SoftwareInstallation;

@Repository
public class JOOQSoftwareInstallationRepository extends RepositoryBaseImpl<SoftwareInstallationRecord, SoftwareInstallation> implements SoftwareInstallationRepository {

	@Autowired
	protected JOOQSoftwareInstallationRepository(DSLContext jooq) {
		super(SOFTWARE_INSTALLATION, SoftwareInstallation.class, jooq);
	}

	@Override
	public SoftwareInstallation findByServerAndSoftware(Server server, Software software) {
		return findByServerAndSoftware(server.getId(), software.getId());
	}

	@Override
	public SoftwareInstallation findByServerAndSoftware(Integer server, Integer software) {
		return jooq.selectFrom(SOFTWARE_INSTALLATION).where(SOFTWARE_INSTALLATION.SERVER_ID.eq(server).and(SOFTWARE_INSTALLATION.SOFTWARE_ID.eq(software))).fetchOneInto(SoftwareInstallation.class);
	}

}
