package com.go2wheel.mysqlbackup.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.BeansException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.go2wheel.mysqlbackup.controller.ControllerBase;

@ConfigurationProperties(prefix = "menus")
@Component
public class MainMenuGroups implements ApplicationContextAware {
	
	private ApplicationContext applicationContext;

	List<MenuGroup> groups = new ArrayList<>();
	
	private boolean cloned = false;

	public List<MenuGroup> getGroups() {
		return groups;
	}

	public void setGroups(List<MenuGroup> groups) {
		this.groups = groups;
	}
	
	public MainMenuGroups clone() {
		MainMenuGroups mgps = new MainMenuGroups();
		mgps.cloned = true;
		List<MenuGroup> clonedGroup = getGroups().stream().map(g -> g.clone()).collect(Collectors.toList());
		Collections.sort(clonedGroup);
		mgps.setGroups(clonedGroup);
		return mgps;
	}
	
	public List<MainMenuItem> getMenuItems() {
		return getGroups().stream().flatMap(g -> g.getItems().stream()).collect(Collectors.toList());
	}
	
	public MainMenuGroups prepare(String currentUri) {
		Assert.isTrue(cloned, "only cloned groups could be used.");
		Optional<MainMenuItem> mi = getGroups().stream().flatMap(g -> g.getItems().stream()).filter(it ->currentUri.equals(it.getPath())).findFirst();
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
	
	
	@PostConstruct
	public void after() {
		Map<String, ? extends ControllerBase> cbs =  applicationContext.getBeansOfType(ControllerBase.class);
		cbs.values().stream().map(cb -> cb.getMenuItems()).filter(Objects::nonNull).flatMap(mis -> mis.stream()).map(mi -> {
			if (!mi.getName().startsWith("menu.")) {
				mi.setName("menu." + mi.getName());
			}
			return mi;
		}).forEach(mi -> {
			Optional<MenuGroup> mgOp = groups.stream().filter(gp -> mi.getGroupName().equals(gp.getName())).findFirst();
			if (!mgOp.isPresent()) {
				MenuGroup mg = new MenuGroup(mi.getGroupName()); 
				groups.add(mg);
				mgOp = Optional.of(mg);
			}
			mgOp.get().getItems().add(mi);
		});
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
