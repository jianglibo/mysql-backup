package com.go2wheel.mysqlbackup.model;

import java.util.Date;

import com.go2wheel.mysqlbackup.util.ObjectUtil;
import com.go2wheel.mysqlbackup.value.ToListRepresentation;
import com.go2wheel.mysqlbackup.yml.YamlInstance;

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
		return YamlInstance.INSTANCE.yaml.dumpAsMap(this);
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	
	@Override
	public String toListRepresentation(String... fields) {
		if (fields.length == 0) {
			return ObjectUtil.dumpObjectAsMap(this);
		}
		return ObjectUtil.toListRepresentation(this, fields); 
	}

}
