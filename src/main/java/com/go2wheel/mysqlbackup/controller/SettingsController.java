package com.go2wheel.mysqlbackup.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.go2wheel.mysqlbackup.MyAppSettings.SshConfig;


@Controller
@RequestMapping(SettingsController.uri)
public class SettingsController  extends ControllerBase {
	
	public static final String uri = "/app/settings";

	@GetMapping("")
	String getPage(Model model) {
		model.addAttribute("sshconfig", new SshConfig());
		return "settings";
	}

	@PostMapping("")
	String postPage(@ModelAttribute SshConfig sshconfig, final BindingResult bindingResult, final Model model) {
		model.addAttribute("sshconfig", sshconfig);
		return "settings";
	}
}
