package com.go2wheel.mysqlbackup.ui;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@ConfigurationProperties(prefix = "menus")
@Component
public class MenuGroups {

	List<MenuGroup> groups;
	
	private boolean cloned = false;

	public List<MenuGroup> getGroups() {
		return groups;
	}

	public void setGroups(List<MenuGroup> groups) {
		this.groups = groups;
	}
	
	public MenuGroups clone() {
		MenuGroups mgps = new MenuGroups();
		mgps.cloned = true;
		List<MenuGroup> clonedGroup = getGroups().stream().map(g -> g.clone()).collect(Collectors.toList());
		Collections.sort(clonedGroup);
		mgps.setGroups(clonedGroup);
		return mgps;
	}
	
	public List<MenuItem> getMenuItems() {
		return getGroups().stream().flatMap(g -> g.getItems().stream()).collect(Collectors.toList());
	}
	
	public MenuGroups prepare(String currentUri) {
		Assert.isTrue(cloned, "only cloned groups could be used.");
		Optional<MenuItem> mi = getGroups().stream().flatMap(g -> g.getItems().stream()).filter(it ->currentUri.equals(it.getPath())).findFirst();
		if (mi.isPresent()) {
			mi.get().setActive(true);
		}
		
		for(int i = 1; i< getGroups().size(); i++) {
			MenuGroup mg = getGroups().get(i);
			if (mg.getItems().size() > 0) {
				mg.getItems().get(0).setGroupFirst(true);
			}
		}
		return this;
	}
}
