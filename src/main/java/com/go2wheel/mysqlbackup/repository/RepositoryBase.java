package com.go2wheel.mysqlbackup.repository;

import java.util.List;

import org.jooq.DAO;
import org.jooq.UpdatableRecord;

import com.go2wheel.mysqlbackup.model.BaseModel;

public interface RepositoryBase<R extends UpdatableRecord<R>, P extends BaseModel> extends DAO<R, P, Integer> {
	
	P insertAndReturn(P p);

	List<P> getRecentItems(int number);

}
