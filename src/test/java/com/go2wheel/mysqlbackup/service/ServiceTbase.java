package com.go2wheel.mysqlbackup.service;

import org.jooq.UpdatableRecord;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.model.BaseModel;

public class ServiceTbase extends SpringBaseFort {
	
	protected String serverHost = "abc";
	
	protected <R extends UpdatableRecord<R>,M extends BaseModel, S extends ServiceBase<R, M>>  void deleteAll(S service) {
		service.findAll().stream().forEach(pojo -> service.delete(pojo));
	}

}
