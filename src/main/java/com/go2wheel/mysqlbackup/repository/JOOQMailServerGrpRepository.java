package com.go2wheel.mysqlbackup.repository;


import static com.go2wheel.mysqlbackup.jooqschema.tables.MailServerGrp.MAIL_SERVER_GRP;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.MailServerGrpRecord;
import com.go2wheel.mysqlbackup.model.MailServerGrp;;

@Repository
public class JOOQMailServerGrpRepository extends RepositoryBaseImpl<MailServerGrpRecord, MailServerGrp> implements MailServerGrpRepository {
	@Autowired
	protected JOOQMailServerGrpRepository(DSLContext jooq) {
		super(MAIL_SERVER_GRP, MailServerGrp.class, jooq);
	}

}
