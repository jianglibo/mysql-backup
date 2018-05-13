package com.go2wheel.mysqlbackup.repository;

import org.jooq.DAO;
import org.jooq.UpdatableRecord;

import com.go2wheel.mysqlbackup.model.BaseModel;

public interface RepositoryBase<R extends UpdatableRecord<R>, P extends BaseModel, T> extends DAO<R, P, T> {
	
	P insertAndReturn(P p);

}
