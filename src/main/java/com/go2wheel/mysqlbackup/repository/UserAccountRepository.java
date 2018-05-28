package com.go2wheel.mysqlbackup.repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.UserAccountRecord;
import com.go2wheel.mysqlbackup.model.UserAccount;

public interface UserAccountRepository extends RepositoryBase<UserAccountRecord, UserAccount>{
	UserAccount findByEmail(String email);
	UserAccount findByMobile(String mobile);
	UserAccount findByName(String name);
}
