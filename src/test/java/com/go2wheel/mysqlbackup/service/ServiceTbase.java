package com.go2wheel.mysqlbackup.service;

import java.util.List;

import org.jooq.UpdatableRecord;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.model.BaseModel;

public class ServiceTbase extends SpringBaseFort {
	
	protected String serverHost = "abc";
	
	public ServiceTbase() {
		super(false);
	}
	
	protected <R extends UpdatableRecord<R>,M extends BaseModel, S extends ServiceBase<R, M>>  void deleteAll(S service) {
		List<M> ms = service.findAll(); 
		ms.stream().forEach(pojo -> service.delete(pojo));
	}

}
