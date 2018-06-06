package com.go2wheel.mysqlbackup.repository;


import static com.go2wheel.mysqlbackup.jooqschema.tables.BigOb.BIG_OB;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.BigObRecord;
import com.go2wheel.mysqlbackup.model.BigOb;

@Repository
public class JOOQBigObRepository extends RepositoryBaseImpl<BigObRecord, BigOb> implements BigObRepository {

	@Autowired
	protected JOOQBigObRepository(DSLContext jooq) {
		super(BIG_OB, BigOb.class, jooq);
	}

	@Override
	public BigOb findByName(String name) {
		return jooq.selectFrom(BIG_OB).where(BIG_OB.NAME.eq(name)).fetchOneInto(BigOb.class);
	}


}
