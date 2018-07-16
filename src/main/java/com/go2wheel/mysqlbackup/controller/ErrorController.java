package com.go2wheel.mysqlbackup.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.go2wheel.mysqlbackup.ui.MainMenuItem;

@Controller
@RequestMapping("/error")
public class ErrorController extends ControllerBase {
	
	
	@GetMapping("")
	public String processError(Model model, HttpServletRequest request) {
        model.addAttribute("status", request.getAttribute("javax.servlet.error.status_code"));
        model.addAttribute("reason", request.getAttribute("javax.servlet.error.message"));
		return "error";
	}

	@Override
	public List<MainMenuItem> getMenuItems() {
		return null;
	}

}
