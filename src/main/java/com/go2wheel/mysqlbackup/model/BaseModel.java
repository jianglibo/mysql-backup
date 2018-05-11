package com.go2wheel.mysqlbackup.model;

import javax.validation.constraints.NotNull;

public class BaseModel {
	
	@NotNull
	private Integer id;

	public Integer  getId() {
		return id;
	}

	public void setId(Integer  id) {
		this.id = id;
	}

}
