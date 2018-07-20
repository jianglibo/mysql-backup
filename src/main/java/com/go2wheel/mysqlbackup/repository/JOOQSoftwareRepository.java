package com.go2wheel.mysqlbackup.repository;


import static com.go2wheel.mysqlbackup.jooqschema.tables.Software.SOFTWARE;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.SoftwareRecord;
import com.go2wheel.mysqlbackup.model.Software;

@Repository
public class JOOQSoftwareRepository extends RepositoryBaseImpl<SoftwareRecord, Software> implements SoftwareRepository {

	@Autowired
	protected JOOQSoftwareRepository(DSLContext jooq) {
		super(SOFTWARE, Software.class, jooq);
	}

}
