package com.go2wheel.mysqlbackup.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.go2wheel.mysqlbackup.service.GlobalStore;
import com.go2wheel.mysqlbackup.ui.MainMenuItemImpl;

@Controller
@RequestMapping(AsyncTasksController.MAPPING_PATH)
public class AsyncTasksController extends ControllerBase {

	public static final String MAPPING_PATH = "/app/asynctasks";

	@Autowired
	private GlobalStore globalStore;

	public AsyncTasksController() {
		super(MAPPING_PATH);
	}

	@GetMapping("")
	public String status(Model model, HttpServletRequest request)
			throws IOException {
		String sid = request.getSession(true).getId();
		model.addAttribute(CRUDController.LIST_OB_NAME, globalStore.getFutureGroupAll(sid));
		model.addAttribute("storeState", globalStore.getStoreState());
		return "global-store-state";
	}
	
	@PostMapping("")
	public String clearCompleted(Model model, HttpServletRequest request)
			throws IOException {
		model.asMap().clear();
		String sid = request.getSession(true).getId();
		globalStore.clearCompleted(sid);
		return "redirect:" + request.getRequestURI();
	}


	@Override
	public MainMenuItemImpl getMenuItem() {
		return new MainMenuItemImpl("appmodel", "async-tasks", getMappingUrl(), 1000);
	}

}
