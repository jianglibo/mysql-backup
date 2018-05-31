package com.go2wheel.mysqlbackup.repository;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.ReuseableCronRecord;
import com.go2wheel.mysqlbackup.model.ReusableCron;

public interface ReusableCronRepository extends RepositoryBase<ReuseableCronRecord, ReusableCron>{

	ReusableCron findByExpression(String expression);

}
