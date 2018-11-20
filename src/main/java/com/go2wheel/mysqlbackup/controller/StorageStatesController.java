package com.go2wheel.mysqlbackup.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.go2wheel.mysqlbackup.dbservice.ServerDbService;
import com.go2wheel.mysqlbackup.dbservice.StorageStateDbService;
import com.go2wheel.mysqlbackup.job.StorageStateJob;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.StorageState;
import com.go2wheel.mysqlbackup.ui.MainMenuItemImpl;
import com.go2wheel.mysqlbackup.value.AjaxDataResult;
import com.go2wheel.mysqlbackup.value.AjaxResult;


@Controller
@RequestMapping(StorageStatesController.MAPPING_PATH)
public class StorageStatesController  extends  CRUDController<StorageState, StorageStateDbService> {
	
	@Autowired
	public StorageStatesController(StorageStateDbService dbService) {
		super(StorageState.class, dbService, MAPPING_PATH);
	}
	public static final String MAPPING_PATH = "/app/storage-states";
	
	@Autowired
	private ServerDbService serverDbService;
	
	@Autowired
	private StorageStateJob storageStateJob;


	@Override
	protected int getMenuOrder() {
		return 100;
	}


	@Override
	boolean copyProperties(StorageState entityFromForm, StorageState entityFromDb) {
		return false;
	}


	@Override
	protected void formAttribute(Model model) {
		
	}
	
//	@PostMapping
//	public ResponseEntity<AjaxResult> createOne(@RequestParam Server server, HttpServletRequest request) {
//		return ResponseEntity.ok(new AjaxDataResult<>(storageStateJob.lockWrapped(server, "fromcontroller")));
//	}
	
	
	@Override
	String getListPage(Model model, HttpServletRequest request) {
		String serverId = request.getParameter("server");
		if (serverId == null) {
			return "redirect:/";
		} else {
			Server server = serverDbService.findById(serverId);
			List<StorageState> StorageStates = getDbService().findByServerId(server.getId());
			model.addAttribute(LIST_OB_NAME, StorageStates);
			model.addAttribute("server", server);
			commonAttribute(model);
			listExtraAttributes(model);
			return getListTpl();
		}
	}


	@Override
	protected void listExtraAttributes(Model model) {
		
	}

	@Override
	public StorageState newModel() {
		return null;
	}
	
	@Override
	public MainMenuItemImpl getMenuItem() {
		return null;
	}

}
