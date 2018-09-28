package com.go2wheel.mysqlbackup.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.ServerState;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.service.ServerStateDbService;
import com.go2wheel.mysqlbackup.ui.MainMenuItemImpl;


@Controller
@RequestMapping(ServerStatesController.MAPPING_PATH)
public class ServerStatesController  extends  CRUDController<ServerState, ServerStateDbService> {
	
	@Autowired
	public ServerStatesController(ServerStateDbService dbService) {
		super(ServerState.class, dbService, MAPPING_PATH);
	}
	public static final String MAPPING_PATH = "/app/server-states";
	
	@Autowired
	private ServerDbService serverDbService;


	@Override
	protected int getMenuOrder() {
		return 100;
	}


	@Override
	boolean copyProperties(ServerState entityFromForm, ServerState entityFromDb) {
		return false;
	}


	@Override
	protected void formAttribute(Model model) {
		
	}
	
	
	@Override
	String getListPage(Model model, HttpServletRequest request) {
		String serverId = request.getParameter("server");
		if (serverId == null) {
			return "redirect:/";
		} else {
			Server server = serverDbService.findById(serverId);
			List<ServerState> serverStates = getDbService().findByServerId(server.getId());
			model.addAttribute(LIST_OB_NAME, serverStates);
			commonAttribute(model);
			listExtraAttributes(model);
			return getListTpl();
		}
	}


	@Override
	protected void listExtraAttributes(Model model) {
		
	}

	@Override
	public ServerState newModel() {
		return null;
	}
	
	@Override
	public MainMenuItemImpl getMenuItem() {
		return null;
	}

}
