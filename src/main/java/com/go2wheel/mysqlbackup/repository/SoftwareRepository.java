package com.go2wheel.mysqlbackup.repository;

import java.util.List;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.SoftwareRecord;
import com.go2wheel.mysqlbackup.model.Software;

public interface SoftwareRepository extends RepositoryBase<SoftwareRecord, Software>{

	Software findByUniqueField(Software software);

	List<Software> findByName(String name);

}
