package com.go2wheel.mysqlbackup.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.go2wheel.mysqlbackup.commands.MysqlService;
import com.go2wheel.mysqlbackup.model.PlayBack;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.ui.MainMenuItem;

@Controller
@RequestMapping(MysqlRestoreController.MAPPING_PATH)
public class MysqlRestoreController extends ControllerBase {
	
	
	public static final String MAPPING_PATH = "/app/mysqlrestore";
	
	@Autowired
	private ServerDbService serverDbService;
	
	@Autowired
	private MysqlService mysqlService;
	
	public MysqlRestoreController() {
		super(MAPPING_PATH);
	}
	
	@GetMapping("/{playback}")
	public String listLocalRepos(@PathVariable PlayBack playback, Model model, HttpServletRequest request) throws IOException {
		Server sourceServer = serverDbService.findById(playback.getSourceServerId());
		model.addAttribute(CRUDController.LIST_OB_NAME, mysqlService.listLocalDumps(sourceServer));
		return "mysql-restore-list";
	}

	@Override
	public List<MainMenuItem> getMenuItems() {
		return null;
	}

}
