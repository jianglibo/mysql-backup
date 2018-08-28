package com.go2wheel.mysqlbackup.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.go2wheel.mysqlbackup.ui.MainMenuItemImpl;

@Controller
@RequestMapping(ErrorController.MAPPING_PATH)
public class ErrorController extends ControllerBase implements org.springframework.boot.web.servlet.error.ErrorController {
	
	public static final String MAPPING_PATH = "/error";
	
	public ErrorController() {
		super(MAPPING_PATH);
	}
	
	@GetMapping("")
	public String processError(Model model, HttpServletRequest request) {
        model.addAttribute("status", request.getAttribute("javax.servlet.error.status_code"));
        model.addAttribute("reason", request.getAttribute("javax.servlet.error.message"));
		return "error";
	}

	@Override
	public MainMenuItemImpl getMenuItem() {
		return null;
	}

	@Override
	public String getErrorPath() {
		return MAPPING_PATH;
	}

}
