package com.go2wheel.mysqlbackup.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import com.go2wheel.mysqlbackup.ui.MenuGroups;
import com.go2wheel.mysqlbackup.ui.MenuItem;


@Controller
public class HomeController  implements ApplicationContextAware {
	
	private ApplicationContext applicationContext;
	
	@Autowired
	private MenuGroups menuGroups;

	@ModelAttribute
	public void populateServerGroup(Model model, HttpServletRequest request) {
		List<MenuItem> items = menuGroups.clone().prepare(request.getRequestURI()).getMenuItems();
		model.addAttribute("menus", items);
	}

	@GetMapping("/")
	String home() {
		return "index";
	}

	@GetMapping("/app/{page}")
	String appPage(@PathVariable String page) {
		return page;
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
