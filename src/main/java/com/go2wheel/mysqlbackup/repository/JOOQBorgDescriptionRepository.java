package com.go2wheel.mysqlbackup.repository;


import static com.go2wheel.mysqlbackup.jooqschema.tables.BorgDescription.BORG_DESCRIPTION;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.BorgDescriptionRecord;
import com.go2wheel.mysqlbackup.model.BorgDescription;

@Repository
public class JOOQBorgDescriptionRepository extends RepositoryBaseImpl<BorgDescriptionRecord, BorgDescription> implements BorgDescriptionRepository {

	@Autowired
	protected JOOQBorgDescriptionRepository(DSLContext jooq) {
		super(BORG_DESCRIPTION, BorgDescription.class, jooq);
	}

	@Override
	public BorgDescription findByServerId(Integer id) {
		return jooq.selectFrom(BORG_DESCRIPTION).where(BORG_DESCRIPTION.SERVER_ID.eq(id)).fetchOneInto(BorgDescription.class);
	}
}
