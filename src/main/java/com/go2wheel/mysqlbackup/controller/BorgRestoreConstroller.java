package com.go2wheel.mysqlbackup.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.go2wheel.mysqlbackup.borg.BorgService;
import com.go2wheel.mysqlbackup.model.PlayBack;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.ui.MainMenuItem;

@Controller
@RequestMapping(BorgRestoreConstroller.MAPPING_PATH)
public class BorgRestoreConstroller extends ControllerBase {
	
	
	public static final String MAPPING_PATH = "/app/borgrestore";
	
	@Autowired
	private ServerDbService serverDbService;
	
	@Autowired
	private BorgService borgService;
	
	public BorgRestoreConstroller() {
		super(MAPPING_PATH);
	}
	
	@GetMapping("/{playback}")
	public String listLocalRepos(@PathVariable PlayBack playback, Model model,  HttpServletRequest request) throws IOException {
		Server sourceServer = serverDbService.findById(playback.getSourceServerId());
		model.addAttribute(CRUDController.LIST_OB_NAME, borgService.listLocalRepos(sourceServer));
		return "borg-restore-list";
	}
	
	@PostMapping("/{playback}")
	public String restore(@PathVariable PlayBack playback, @RequestParam(name="repo") String repo, Model model,  HttpServletRequest request) {
//		borgPlayback.playback(session, localReop, pb);
		return null;
	}



	
	@Override
	public List<MainMenuItem> getMenuItems() {
		return null;
	}

}
