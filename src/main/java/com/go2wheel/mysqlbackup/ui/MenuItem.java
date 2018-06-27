package com.go2wheel.mysqlbackup.ui;

public class MenuItem implements Comparable<MenuItem> {
	
	private static final String DIVIDED = "menu-item-divided";
	private static final String ACTIVE = "menu-item-divided";
	
	private String name;
	
	private Integer order = 0;
	
	private String path;
	
	private boolean active = false;
	
	private boolean groupFirst = false;
	
//	public String extraClass() {
//		
//	}
	
	public MenuItem clone() {
		MenuItem mi = new MenuItem();
		mi.setName(getName());
		mi.setPath(getPath());
		mi.setOrder(getOrder());
		return mi;
	}

	@Override
	public int compareTo(MenuItem o) {
		return this.getOrder().compareTo(o.getOrder());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isGroupFirst() {
		return groupFirst;
	}

	public void setGroupFirst(boolean groupFirst) {
		this.groupFirst = groupFirst;
	}

}
