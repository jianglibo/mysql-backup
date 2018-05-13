package com.go2wheel.mysqlbackup.repository;


import static com.go2wheel.mysqlbackup.jooqschema.tables.MailAddress.MAIL_ADDRESS;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.MailAddressRecord;
import com.go2wheel.mysqlbackup.model.MailAddress;

@Repository
public class JOOQMailAddressRepository extends RepositoryBaseImpl<MailAddressRecord, MailAddress> implements MailAddressRepository {

	@Autowired
	protected JOOQMailAddressRepository(DSLContext jooq) {
		super(MAIL_ADDRESS, MailAddress.class, jooq);
	}
}
