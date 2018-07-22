package com.go2wheel.mysqlbackup.value;

import java.util.List;

import com.google.common.collect.Lists;

public class AjaxDataResult {
	
	private List<Object> data = Lists.newArrayList();
	
	public void addObject(Object o) {
		this.data.add(o);
	}

	public List<Object> getData() {
		return data;
	}

	public void setData(List<Object> data) {
		this.data = data;
	}

}
