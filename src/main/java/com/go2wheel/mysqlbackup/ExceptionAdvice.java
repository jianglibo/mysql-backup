package com.go2wheel.mysqlbackup;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.go2wheel.mysqlbackup.exception.CommandNotFoundException;
import com.go2wheel.mysqlbackup.ui.MainMenuGroups;
import com.go2wheel.mysqlbackup.ui.MainMenuItem;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.jcraft.jsch.JSchException;

@ControllerAdvice
public class ExceptionAdvice {
	
	@Autowired
	private MainMenuGroups menuGroups;
	
	@ExceptionHandler(JSchException.class)
	public String exception(JSchException e, Model model) {
		model.addAttribute("exp", e);
		return "error-jsch";
	}
	
	@ExceptionHandler(CommandNotFoundException.class)
	public String exception(CommandNotFoundException e, Model model) {
		model.addAttribute("exp", e);
		return "error-command";
	}
	
	@ExceptionHandler
	public String exceptionHandler(Exception ex, Model model, HttpServletRequest request) {
		populateMainMenu(model, request);
		model.addAttribute("exception",ex);
		model.addAttribute("stacktrace", ExceptionUtil.stackTraceToString(ex));
		return "error-exception";
	}
	
	private void populateMainMenu(Model model, HttpServletRequest request) {
		List<MainMenuItem> items = menuGroups.clone().prepare(request.getRequestURI()).getMenuItems();
		model.addAttribute("menus", items);
	}

}
