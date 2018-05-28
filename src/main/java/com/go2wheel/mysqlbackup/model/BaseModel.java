package com.go2wheel.mysqlbackup.model;

import com.go2wheel.mysqlbackup.util.ObjectUtil;

public class BaseModel {
	
	private Integer id;

	public Integer  getId() {
		return id;
	}

	public void setId(Integer  id) {
		this.id = id;
	}
	
	
	@Override
	public String toString() {
		return ObjectUtil.dumpObjectAsMap(this);
	}

}
