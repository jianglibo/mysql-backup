package com.go2wheel.mysqlbackup.jooq;

import org.jooq.RecordMapper;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.ReuseableCronRecord;
import com.go2wheel.mysqlbackup.model.ReusableCron;

public class ReuableCronRecordMapper implements RecordMapper<ReuseableCronRecord, ReusableCron> {

	@Override
	public ReusableCron map(ReuseableCronRecord record) {
		ReusableCron uc = new ReusableCron();
		uc.setDescription(record.getDescription());
		uc.setExpression(record.getExpression());
		uc.setId(record.getId());
		return uc;
	}

}
