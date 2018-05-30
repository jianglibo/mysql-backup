package com.go2wheel.mysqlbackup.model;

import java.util.Date;

import com.go2wheel.mysqlbackup.util.ObjectUtil;
import com.go2wheel.mysqlbackup.value.ToListRepresentation;

public abstract class BaseModel implements ToListRepresentation{
	
	private Integer id;
	
	private Date createdAt;
	
	public BaseModel() {
		this.createdAt = new Date();
	}

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

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

}
