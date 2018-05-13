package com.go2wheel.mysqlbackup.repository;

import org.jooq.DSLContext;
import org.jooq.Table;
import org.jooq.UpdatableRecord;
import org.jooq.impl.DAOImpl;

import com.go2wheel.mysqlbackup.model.BaseModel;

public abstract class RepositoryBaseImpl<R extends UpdatableRecord<R>, P extends BaseModel> extends DAOImpl<R, P, Integer> implements RepositoryBase<R, P, Integer>{
	
	protected DSLContext jooq;

	protected RepositoryBaseImpl(Table<R> table, Class<P> type, DSLContext jooq) {
		super(table, type, jooq.configuration());
		this.jooq = jooq;
	}
	
	@Override
	protected Integer getId(P object) {
		return object.getId();
	}
	
	@Override
	public P insertAndReturn(P pojo) {
		R record = jooq.newRecord(getTable(), pojo);
		record.store();
		return record.into(getType());
	}

}
