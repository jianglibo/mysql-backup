package com.go2wheel.mysqlbackup.jooq;

import org.jooq.RecordMapper;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.MailaddressRecord;
import com.go2wheel.mysqlbackup.model.MailAddress;

public class MailAddressRecordMapper implements RecordMapper<MailaddressRecord, MailAddress> {

	@Override
	public MailAddress map(MailaddressRecord record) {
		MailAddress ma = new MailAddress();
		ma.setDescription(record.getDescription());
		ma.setEmail(record.getEmail());
		ma.setId(record.getId());
		return ma;
	}

}
