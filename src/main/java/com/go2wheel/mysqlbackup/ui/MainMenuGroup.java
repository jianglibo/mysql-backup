package com.go2wheel.mysqlbackup.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.MoreObjects;

public class MainMenuGroup implements Comparable<MainMenuGroup>, Cloneable {

	private String name;

	private Integer order = 0;

	private List<MainMenuItem> items = new ArrayList<>();

	public MainMenuGroup() {
	}

	public MainMenuGroup(String name) {
		this.name = name;
	}

	public MainMenuGroup clone() {
		MainMenuGroup nmg = new MainMenuGroup();
		nmg.setName(getName());
		nmg.setOrder(getOrder());
		nmg.setItems(getItems().stream().map(it -> it.clone()).collect(Collectors.toList()));
		return nmg;
	}

	@Override
	public int compareTo(MainMenuGroup o) {
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

	public List<MainMenuItem> getItems() {
		return items;
	}

	public void setItems(List<MainMenuItem> items) {
		this.items = items;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("name", getName()).add("order", getOrder())
				.add("items", getItems().size()).toString();
	}

}
