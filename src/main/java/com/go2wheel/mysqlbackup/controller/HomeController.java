package com.go2wheel.mysqlbackup.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.service.TemplateContextService;
import com.go2wheel.mysqlbackup.ui.MainMenuItem;


@Controller
public class HomeController extends ControllerBase {
	
	@Autowired
	private TemplateContextService templateContextService;
	
	@Autowired
	private ServerDbService serverDbService;

	@ModelAttribute
	public void populateMainMenu(Model model, HttpServletRequest request) {
		Server myself = serverDbService.findByHost("localhost");
		model.addAttribute("myself", templateContextService.prepareServerContext(myself));
	}

	@GetMapping("/")
	String home() {
		return "index";
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public List<MainMenuItem> getMenuItems() {
		return null;
	}

}
