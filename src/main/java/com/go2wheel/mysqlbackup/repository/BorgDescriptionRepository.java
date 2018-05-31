package com.go2wheel.mysqlbackup.repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.BorgDescriptionRecord;
import com.go2wheel.mysqlbackup.model.BorgDescription;

public interface BorgDescriptionRepository extends RepositoryBase<BorgDescriptionRecord, BorgDescription>{

	BorgDescription findByServerId(Integer id);

}
