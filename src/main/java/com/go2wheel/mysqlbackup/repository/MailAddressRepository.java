package com.go2wheel.mysqlbackup.repository;

import org.jooq.DAO;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.MailAddressRecord;
import com.go2wheel.mysqlbackup.model.MailAddress;

public interface MailAddressRepository extends DAO<MailAddressRecord, MailAddress, Integer>{

}
