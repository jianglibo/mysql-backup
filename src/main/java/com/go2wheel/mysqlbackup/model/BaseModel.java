package com.go2wheel.mysqlbackup.model;

import com.go2wheel.mysqlbackup.util.ObjectUtil;
import com.go2wheel.mysqlbackup.value.ToListRepresentation;

public abstract class BaseModel implements ToListRepresentation{
	
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
