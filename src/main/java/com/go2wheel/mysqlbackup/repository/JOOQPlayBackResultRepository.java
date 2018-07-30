package com.go2wheel.mysqlbackup.repository;


import static com.go2wheel.mysqlbackup.jooqschema.tables.PlayBackResult.PLAY_BACK_RESULT;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.PlayBackResultRecord;
import com.go2wheel.mysqlbackup.model.PlayBackResult;

@Repository
public class JOOQPlayBackResultRepository extends RepositoryBaseImpl<PlayBackResultRecord, PlayBackResult> implements PlayBackResultRepository {

	@Autowired
	protected JOOQPlayBackResultRepository(DSLContext jooq) {
		super(PLAY_BACK_RESULT, PlayBackResult.class, jooq);
	}

}
