package com.go2wheel.mysqlbackup.ui;

import java.util.List;
import java.util.stream.Collectors;

public class MenuGroup implements Comparable<MenuGroup>{
	
	private String name;
	
	private Integer order = 0;
	
	private List<MenuItem> items;
	
	public MenuGroup clone() {
		MenuGroup nmg = new MenuGroup();
		nmg.setName(getName());
		nmg.setOrder(getOrder());
		nmg.setItems(getItems().stream().map(it -> it.clone()).collect(Collectors.toList()));
		return nmg;
	}

	@Override
	public int compareTo(MenuGroup o) {
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

	public List<MenuItem> getItems() {
		return items;
	}

	public void setItems(List<MenuItem> items) {
		this.items = items;
	}
	
	

}
