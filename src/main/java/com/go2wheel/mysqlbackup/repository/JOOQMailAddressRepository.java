package com.go2wheel.mysqlbackup.repository;

import static com.go2wheel.mysqlbackup.jooqschema.tables.Mailaddress.MAILADDRESS;

import org.jooq.DSLContext;
import org.jooq.impl.DAOImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.MailaddressRecord;
import com.go2wheel.mysqlbackup.model.MailAddress;

@Repository
public class JOOQMailAddressRepository extends DAOImpl<MailaddressRecord, MailAddress, Integer> implements MailAddressRepository {

	@Autowired
	protected JOOQMailAddressRepository(DSLContext jooq) {
		super(MAILADDRESS, MailAddress.class, jooq.configuration());
	}

	@Override
	protected Integer getId(MailAddress object) {
		return object.getId();
	}
}
