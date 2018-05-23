package com.go2wheel.mysqlbackup.service;

import java.util.List;

import javax.validation.Valid;

import org.jooq.UpdatableRecord;

import com.go2wheel.mysqlbackup.model.BaseModel;
import com.go2wheel.mysqlbackup.repository.RepositoryBase;

public abstract class ServiceBase<R extends UpdatableRecord<R>, P extends BaseModel> {
	
	protected RepositoryBase<R, P, Integer> repo;
	
	public ServiceBase(RepositoryBase<R, P, Integer> repo) {
		this.repo = repo;
	}
	
	public P save(@Valid P pojo) {
		return repo.insertAndReturn(pojo);
	}
	
	public List<P> findAll() {
		return repo.findAll();
	}
	
	public void delete(P pojo) {
		repo.delete(pojo);
	}
	
	public long count() {
		return repo.count();
	}
	
	public void deteteAll() {
		findAll().forEach(item -> delete(item));
	}

}
