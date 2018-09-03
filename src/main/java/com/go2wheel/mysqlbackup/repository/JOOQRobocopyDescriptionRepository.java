package com.go2wheel.mysqlbackup.repository;


import static com.go2wheel.mysqlbackup.jooqschema.tables.RobocopyDescription.ROBOCOPY_DESCRIPTION;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.RobocopyDescriptionRecord;
import com.go2wheel.mysqlbackup.model.RobocopyDescription;

@Repository
public class JOOQRobocopyDescriptionRepository extends RepositoryBaseImpl<RobocopyDescriptionRecord, RobocopyDescription> implements RobocopyDescriptionRepository {

	@Autowired
	protected JOOQRobocopyDescriptionRepository(DSLContext jooq) {
		super(ROBOCOPY_DESCRIPTION, RobocopyDescription.class, jooq);
	}

	@Override
	public RobocopyDescription findByServerId(Integer id) {
		return jooq.selectFrom(ROBOCOPY_DESCRIPTION).where(ROBOCOPY_DESCRIPTION.SERVER_ID.eq(id)).fetchOneInto(RobocopyDescription.class);
	}
}
