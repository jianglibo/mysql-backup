package com.go2wheel.mysqlbackup.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.MailAddressRecord;
import com.go2wheel.mysqlbackup.model.MailAddress;
import com.go2wheel.mysqlbackup.repository.MailAddressRepository;

@Service
@Validated
public class MailAddressService extends ServiceBase<MailAddressRecord, MailAddress> {

	@Autowired
	public MailAddressService(MailAddressRepository mailAddressRepository) {
		super(mailAddressRepository);
	}
	
	
}
