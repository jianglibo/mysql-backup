package com.go2wheel.mysqlbackup.repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.RobocopyDescriptionRecord;
import com.go2wheel.mysqlbackup.model.RobocopyDescription;

public interface RobocopyDescriptionRepository extends RepositoryBase<RobocopyDescriptionRecord, RobocopyDescription>{

	RobocopyDescription findByServerId(Integer id);

}
