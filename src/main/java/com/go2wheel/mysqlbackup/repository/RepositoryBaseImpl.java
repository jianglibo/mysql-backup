package com.go2wheel.mysqlbackup.repository;

import org.jooq.Configuration;
import org.jooq.Table;
import org.jooq.UpdatableRecord;
import org.jooq.impl.DAOImpl;

import com.go2wheel.mysqlbackup.model.BaseModel;

public abstract class RepositoryBaseImpl<R extends UpdatableRecord<R>, P extends BaseModel> extends DAOImpl<R, P, Integer>{

	protected RepositoryBaseImpl(Table<R> table, Class<P> type, Configuration configuration) {
		super(table, type, configuration);
	}
	
	@Override
	protected Integer getId(P object) {
		return object.getId();
	}

}
