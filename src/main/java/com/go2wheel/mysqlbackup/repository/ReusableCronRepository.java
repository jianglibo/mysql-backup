package com.go2wheel.mysqlbackup.repository;

import org.jooq.DAO;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.ReuseableCronRecord;
import com.go2wheel.mysqlbackup.model.ReusableCron;

public interface ReusableCronRepository extends DAO<ReuseableCronRecord, ReusableCron, Integer>{

	ReusableCron insertAndReturn(ReusableCron reusableCron);

}
