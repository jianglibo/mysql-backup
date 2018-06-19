package com.go2wheel.mysqlbackup.repository;


import static com.go2wheel.mysqlbackup.jooqschema.tables.PlayBack.PLAY_BACK;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.PlayBackRecord;
import com.go2wheel.mysqlbackup.model.PlayBack;


@Repository
public class JOOQPlayBackRepository extends RepositoryBaseImpl<PlayBackRecord, PlayBack> implements PlayBackRepository {

	@Autowired
	protected JOOQPlayBackRepository(DSLContext jooq) {
		super(PLAY_BACK, PlayBack.class, jooq);
	}

}
