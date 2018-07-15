package com.go2wheel.mysqlbackup.repository;

import java.util.List;

import org.jooq.Condition;
import org.jooq.DAO;
import org.jooq.SortField;
import org.jooq.UpdatableRecord;

import com.go2wheel.mysqlbackup.model.BaseModel;
import com.go2wheel.mysqlbackup.model.Server;

public interface RepositoryBase<R extends UpdatableRecord<R>, P extends BaseModel> extends DAO<R, P, Integer> {
	
	P insertAndReturn(P p);

	List<P> getRecentItems(Server server, int number);
	
	List<P> findAll(int offset, int limit);
	
	List<P> findAll(SortField<?> sort, int offset, int limit);

	List<P> findAll(Condition eq, int offset, int limit);

	List<P> getRecentItems(int number);
	
	List<P> findByIds(Integer[] array);

}
