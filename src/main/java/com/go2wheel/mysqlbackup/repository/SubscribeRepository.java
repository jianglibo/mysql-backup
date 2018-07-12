package com.go2wheel.mysqlbackup.repository;

import java.util.List;

import javax.validation.constraints.NotNull;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.SubscribeRecord;
import com.go2wheel.mysqlbackup.model.ServerGrp;
import com.go2wheel.mysqlbackup.model.UserAccount;
import com.go2wheel.mysqlbackup.model.Subscribe;

public interface SubscribeRepository extends RepositoryBase<SubscribeRecord, Subscribe>{

	List<Subscribe> findByUser(UserAccount ua);

	Subscribe findByUserAndServerGrp(@NotNull UserAccount user, ServerGrp serverGroup);

}
