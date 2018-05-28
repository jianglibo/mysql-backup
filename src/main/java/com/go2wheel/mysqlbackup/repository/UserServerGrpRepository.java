package com.go2wheel.mysqlbackup.repository;

import java.util.List;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.UserAndServerGrpRecord;
import com.go2wheel.mysqlbackup.model.UserAccount;
import com.go2wheel.mysqlbackup.model.UserServerGrp;

public interface UserServerGrpRepository extends RepositoryBase<UserAndServerGrpRecord, UserServerGrp>{

	List<UserServerGrp> findByUser(UserAccount ua);

}
