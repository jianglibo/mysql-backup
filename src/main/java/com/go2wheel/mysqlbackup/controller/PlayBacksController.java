package com.go2wheel.mysqlbackup.controller;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;

import com.go2wheel.mysqlbackup.model.PlayBack;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.propertyeditor.ListStringToLinesEditor;
import com.go2wheel.mysqlbackup.service.PlayBackDbService;
import com.go2wheel.mysqlbackup.service.ServerDbService;


@Controller
@RequestMapping(PlayBacksController.MAPPING_PATH)
public class PlayBacksController  extends CRUDController<PlayBack, PlayBackDbService> {
	
	public static final String MAPPING_PATH = "/app/play-backs";
	
	@Autowired
	private ServerDbService serverDbService;
	
	@Autowired
	public PlayBacksController(PlayBackDbService dbService) {
		super(PlayBack.class, dbService, MAPPING_PATH);
	}
	
    @InitBinder
    public void initBinder(WebDataBinder binder) {
    	binder.registerCustomEditor(List.class, new ListStringToLinesEditor());
    }

	@Override
	boolean copyProperties(PlayBack entityFromForm, PlayBack entityFromDb) {
		return true;
	}
	
	@GetMapping("/create")
	@Override
	String getCreate(Model model, HttpServletRequest httpRequest) {
		String serverId = httpRequest.getParameter("server");
		Server server = null;
		if (serverId != null) {
			server = serverDbService.findById(serverId);
		}
		PlayBack playback = newModel();
		if (server != null) {
			playback.setTargetServerId(server.getId());
		}
		model.addAttribute(OB_NAME, playback);
		model.addAttribute("editting", false);
		commonAttribute(model);
		formAttribute(model);
		return getFormTpl();
	}

	@Override
	public PlayBack newModel() {
		return new PlayBack();
	}

	@Override
	protected void formAttribute(Model model) {
		model.addAttribute("getServers", serverDbService.findByRole("GET"));
		model.addAttribute("setServers", serverDbService.findByRole("SET"));
	}

	@Override
	protected void listExtraAttributes(Model model) {
		model.addAttribute(ID_ENTITY_MAP, serverDbService.findAll().stream().collect(Collectors.toMap(Server::getId, s -> s)));
	}
	
	protected int getMenuOrder() {
		return 600;
	}
}
