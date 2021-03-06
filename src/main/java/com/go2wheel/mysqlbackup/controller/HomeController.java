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
import com.go2wheel.mysqlbackup.ui.MainMenuItemImpl;
import com.go2wheel.mysqlbackup.util.TplUtil;


@Controller(HomeController.MAPPING_PATH)
public class HomeController extends ControllerBase {
	

	public static final String MAPPING_PATH = "/";
	
	public HomeController() {
		super(MAPPING_PATH);
	}
	
	@Autowired
	private TemplateContextService templateContextService;
	
	@Autowired
	private ServerDbService serverDbService;

	@ModelAttribute
	public void populateContext(Model model, HttpServletRequest request) {
		Server myself = serverDbService.findByHost("localhost");
		model.addAttribute("myself", templateContextService.prepareServerContext(myself));
		model.addAttribute("tplUtil", new TplUtil());
	}

	@GetMapping("")
	String home() {
		return "index";
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public MainMenuItemImpl getMenuItem() {
		return null;
	}

}
