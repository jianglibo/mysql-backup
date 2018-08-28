package com.go2wheel.mysqlbackup.ui;

public interface MainMenuItem extends Comparable<MainMenuItem> {
	String getName();
	String getPath();
	Integer getOrder();
	boolean isActive();
	boolean isGroupFirst();
	void alterState(String currentUrl);
	
	@Override
	default public int compareTo(MainMenuItem o) {
		return this.getOrder().compareTo(o.getOrder());
	}
	void setGroupFirst(boolean b);
	MainMenuItem clone();
	void setGroupName(String name);
	void setName(String name);
	String getGroupName();
}
