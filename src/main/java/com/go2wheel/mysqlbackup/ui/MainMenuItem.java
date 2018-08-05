package com.go2wheel.mysqlbackup.ui;

public class MainMenuItem implements Comparable<MainMenuItem>, Cloneable {
	
//	private static final String DIVIDED = "menu-item-divided";
//	private static final String ACTIVE = "menu-item-divided";
	
	private String name;
	
	private Integer order = 0;
	
	private String path;
	/**
	 * groupName is a necessary, Cause there need a way for controller to define which group it belongs to.
	 */
	private String groupName;
	
	private boolean active = false;
	
	private boolean groupFirst = false;
	
	public MainMenuItem(String groupName, String name, String path, Integer order) {
		this.groupName = groupName;
		this.name = name;
		this.path = path;
		this.order = order;
	}
	
	public MainMenuItem() {
	}

	public MainMenuItem clone() {
		MainMenuItem mi = new MainMenuItem();
		mi.setName(getName());
		mi.setPath(getPath());
		mi.setOrder(getOrder());
		return mi;
	}

	@Override
	public int compareTo(MainMenuItem o) {
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

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

}
