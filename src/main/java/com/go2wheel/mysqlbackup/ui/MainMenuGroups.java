package com.go2wheel.mysqlbackup.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.go2wheel.mysqlbackup.controller.ControllerBase;
import com.go2wheel.mysqlbackup.yml.YamlInstance;

@Component
public class MainMenuGroups implements ApplicationContextAware, Cloneable {

	private ApplicationContext applicationContext;
	
	private Logger logger = LoggerFactory.getLogger(getClass());

	List<MainMenuGroup> groups = new ArrayList<>();

	private boolean cloned = false;

	public List<MainMenuGroup> getGroups() {
		return groups;
	}

	public void setGroups(List<MainMenuGroup> groups) {
		this.groups = groups;
	}

	public MainMenuGroups clone() {
		MainMenuGroups mgps = new MainMenuGroups();
		mgps.cloned = true;
		List<MainMenuGroup> clonedGroups = getGroups().stream().map(g -> g.clone()).collect(Collectors.toList());
		Collections.sort(clonedGroups);
		mgps.setGroups(clonedGroups);
		return mgps;
	}

	public List<MainMenuItem> getMenuItems() {
		return getGroups().stream().flatMap(g -> g.getItems().stream()).collect(Collectors.toList());
	}

	public MainMenuGroups prepare(String currentUri) {
		Assert.isTrue(cloned, "only cloned groups could be used.");
		getGroups().stream().flatMap(g -> g.getItems().stream()).forEach(mi -> mi.alterState(currentUri));

		for (int i = 1; i < getGroups().size(); i++) {
			MainMenuGroup mg = getGroups().get(i);
			if (mg.getItems().size() > 0) {
				mg.getItems().get(0).setGroupFirst(true);
			}
		}
		return this;
	}

	/**
	 * we get menuitem from the application.properties file, at same time merge the
	 * defines from controller, which in controller has higher priority.
	 */
	@PostConstruct
	public void after() {
		// add groupName to menu-item in application.properties file.
		getGroups().forEach(g -> {
			g.getItems().forEach(it -> {
				it.setGroupName(g.getName());
				if (!it.getName().startsWith("menu.")) {
					it.setName("menu." + it.getName());
				}
			});
		});
		
		Map<String, ? extends ControllerBase> cbs = applicationContext.getBeansOfType(ControllerBase.class);
		cbs.values().stream().map(cb -> cb.getMenuItem()).filter(Objects::nonNull)
				.map(mi -> {
					if (!mi.getName().startsWith("menu.")) {
						mi.setName("menu." + mi.getName());
					}
					return mi;
				}).forEach(mi -> {
					Optional<MainMenuGroup> mgOp = groups.stream().filter(gp -> mi.getGroupName().equals(gp.getName()))
							.findFirst();
					if (!mgOp.isPresent()) {
						MainMenuGroup mg = new MainMenuGroup(mi.getGroupName());
						groups.add(mg);
						mgOp = Optional.of(mg);
					}
					mgOp.get().getItems().add(mi);
				});
		
		// remove duplication items.
		getGroups().forEach(g -> {
			Map<String, List<MainMenuItem>> m = g.getItems().stream().collect(Collectors.groupingBy(mi -> {
				return mi.getName();
			}));
			
			List<MainMenuItem> lm = m.values().stream().map(list -> list.get(0)).collect(Collectors.toList());
			Collections.sort(lm);
			g.setItems(lm);
		});
		logger.info(YamlInstance.INSTANCE.yaml.dumpAsMap(groups));
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
