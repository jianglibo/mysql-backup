package com.go2wheel.mysqlbackup.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.ui.MainMenuItem;
import com.go2wheel.mysqlbackup.valueprovider.SharedValueProviderMethods;
import com.google.common.collect.Sets;


@Controller
@RequestMapping(ServersController.uri)
public class ServersController  extends ControllerBase {
	
	public static final String uri = "/app/servers";
	public static final String LIST_OB_NAME = "servers";
	public static final String OB_NAME = "server";
	
	private static final String FORM_TPL = "server-form";
	private static final String LIST_TPL = "servers";
	
	@Autowired
	private ServerDbService serverDbService;
	
	
	private Set<String> getOses() {
		List<String> oses = serverDbService.findDistinctOsType("");
		Set<String> orderedUnique = Sets.newTreeSet();
		orderedUnique.addAll(oses);
		orderedUnique.addAll(SharedValueProviderMethods.predefines);
		return orderedUnique;
	}
	
	@GetMapping("")
	String getPage(Model model) {
		List<Server> servers = serverDbService.findAll();
		model.addAttribute(LIST_OB_NAME, servers);
		return LIST_TPL;
	}
	
	@GetMapping("/create")
	String getCreate(Model model) {
		model.addAttribute(OB_NAME, new Server());
		model.addAttribute("oses", getOses());
		return FORM_TPL;
	}
	
	@PostMapping("/create")
	String postCreate(@Validated @ModelAttribute(OB_NAME) Server server, final BindingResult bindingResult,Model model, RedirectAttributes ras) {
	    if (bindingResult.hasErrors()) {
	    	model.addAttribute("oses", getOses());
	        return FORM_TPL;
	    }
	    
	    ras.addFlashAttribute("formProcessSuccessed", true);
	    return "redirect:" + uri;
	}

	@Override
	public List<MainMenuItem> getMenuItems() {
		return Arrays.asList(new MainMenuItem("appmodel", "servers", uri, 100));
	}
}
