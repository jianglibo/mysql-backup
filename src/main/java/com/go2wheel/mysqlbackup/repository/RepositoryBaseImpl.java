package com.go2wheel.mysqlbackup.repository;

import java.util.Arrays;
import java.util.List;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SortField;
import org.jooq.Table;
import org.jooq.UpdatableRecord;
import org.jooq.impl.DAOImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.go2wheel.mysqlbackup.model.BaseModel;

public abstract class RepositoryBaseImpl<R extends UpdatableRecord<R>, P extends BaseModel> extends DAOImpl<R, P, Integer> implements RepositoryBase<R, P>{
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	protected DSLContext jooq;

	protected RepositoryBaseImpl(Table<R> table, Class<P> type, DSLContext jooq) {
		super(table, type, jooq.configuration());
		this.jooq = jooq;
	}
	
	@Override
	protected Integer getId(P object) {
		return object.getId();
	}
	
	
	public List<P> getRecentItems(int number) {
		Field<?> createdSort =  getTable().field("CREATED_AT");
		return jooq.selectFrom(getTable()).orderBy(createdSort.desc()).limit(number).fetchInto(getType());
	}
	
	@Override
	public List<P> findAll(int offset, int limit) {
		return jooq.selectFrom(getTable()).orderBy(getTable().field("CREATED_AT").desc()).offset(offset).limit(limit).fetchInto(getType());
	}
	
	@Override
	public List<P> findAll(SortField<?> sort, int offset, int limit) {
		return jooq.selectFrom(getTable()).orderBy(sort).offset(offset).limit(limit).fetchInto(getType());
	}
	
	public 	List<P> findAll(Condition eq, int offset, int limit) {
		return jooq.selectFrom(getTable()).where(eq).orderBy(getTable().field("CREATED_AT").desc()).offset(offset).limit(limit).fetchInto(getType());
	}
	
	@Override
	public List<P> findByIds(Integer[] array) {
		Field<?> idf = getTable().field("ID");
		return jooq.selectFrom(getTable()).where(idf.in(Arrays.asList(array))).fetchInto(getType());
	}
	
	@Override
	public List<P> findAllSortByCreatedAtDesc() {
		SortField<?> sf = getTable().field("CREATED_AT").desc();
		return findAll(sf, 0, 1000);
	}
	
	@Override
	public P insertAndReturn(P pojo) {
		if (pojo.getId() != null) {
			update(pojo);
			return pojo;
		}
		R record = jooq.newRecord(getTable(), pojo);
		record.store();
		return record.into(getType());
	}

}
