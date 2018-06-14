package com.go2wheel.mysqlbackup.service;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Result;
import org.jooq.impl.TableImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SqlService {

	@Autowired
	private TableDiscovery tableDiscovery;
	
	@Autowired
	private DSLContext jooq;
	
	public String select(String tableName, int limit) {
		TableImpl<?> ti = tableDiscovery.getTable(tableName);
		Result<?> ro = jooq.selectFrom(ti).limit(limit).fetch();
		return ro.toString();
	}
	
	public int delete(String tableName, int id) {
		TableImpl<?> ti = tableDiscovery.getTable(tableName);
		Field<Integer> fi = (Field<Integer>) ti.field("ID");
		int i = jooq.deleteFrom(ti).where(fi.eq(id)).execute();
		return i;
	}

}
