package com.go2wheel.mysqlbackup.repository;


import static com.go2wheel.mysqlbackup.jooqschema.tables.Diskfree.DISKFREE;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.DiskfreeRecord;
import com.go2wheel.mysqlbackup.model.Diskfree;

@Repository
public class JOOQDiskfreeRepository extends RepositoryBaseImpl<DiskfreeRecord, Diskfree> implements DiskfreeRepository {

	@Autowired
	protected JOOQDiskfreeRepository(DSLContext jooq) {
		super(DISKFREE, Diskfree.class, jooq);
	}
}
