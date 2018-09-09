package com.go2wheel.mysqlbackup.repository;

import java.util.List;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.RobocopyItemRecord;
import com.go2wheel.mysqlbackup.model.RobocopyItem;

public interface RobocopyItemRepository extends RepositoryBase<RobocopyItemRecord, RobocopyItem>{

	List<RobocopyItem> findByDescriptionId(int descriptionId);


}
