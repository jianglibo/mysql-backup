package com.go2wheel.mysqlbackup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.MailServerGrpRecord;
import com.go2wheel.mysqlbackup.model.MailServerGrp;
import com.go2wheel.mysqlbackup.repository.MailServerGrpRepository;

@Service
@Validated
public class MailServerGrpService extends ServiceBase<MailServerGrpRecord, MailServerGrp> {

	@Autowired
	public MailServerGrpService(MailServerGrpRepository mailServerGrpRepository) {
		super(mailServerGrpRepository);
	}
	
}
