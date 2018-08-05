package com.go2wheel.mysqlbackup.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.go2wheel.mysqlbackup.commands.MysqlService;
import com.go2wheel.mysqlbackup.exception.AppNotStartedException;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.exception.ScpException;
import com.go2wheel.mysqlbackup.exception.UnExpectedContentException;
import com.go2wheel.mysqlbackup.model.PlayBack;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.ui.MainMenuItem;
import com.go2wheel.mysqlbackup.value.MycnfFileHolder;
import com.go2wheel.mysqlbackup.value.MysqlDumpFolder;
import com.jcraft.jsch.JSchException;

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
	public String listLocalDumps(@PathVariable PlayBack playback, Model model, HttpServletRequest request) throws IOException {
		Server sourceServer = serverDbService.findById(playback.getSourceServerId());
		Server targetServer = serverDbService.findById(playback.getTargetServerId());
		Path dumps = settingsInDb.getCurrentDumpDir(sourceServer).getParent();
		List<MysqlDumpFolder> dumpFolders = Files.list(dumps).map(MysqlDumpFolder::newInstance).filter(Objects::nonNull).collect(Collectors.toList());
		Collections.sort(dumpFolders);
		model.addAttribute("sourceServer", sourceServer);
		model.addAttribute("targetServer", targetServer);
		model.addAttribute(CRUDController.LIST_OB_NAME, dumpFolders);
		return "mysql-restore-list";
	}
	
	
	@PostMapping("/{playback}")
	public String playback(@PathVariable PlayBack playback,@RequestParam(name="dump") String dumpFolder, Model model, HttpServletRequest request) throws IOException, RunRemoteCommandException, UnExpectedContentException, JSchException, AppNotStartedException, ScpException {
		Server sourceServer = serverDbService.findById(playback.getSourceServerId());
		Server targetServer = serverDbService.findById(playback.getTargetServerId());
		

		
		mysqlService.restore(playback, sourceServer, targetServer, dumpFolder);
		
		
		
		Path dumps = settingsInDb.getCurrentDumpDir(sourceServer).getParent();
		List<MysqlDumpFolder> dumpFolders = Files.list(dumps).map(MysqlDumpFolder::newInstance).filter(Objects::nonNull).collect(Collectors.toList());
		Collections.sort(dumpFolders);
		model.addAttribute("sourceServer", sourceServer);
		model.addAttribute("targetServer", targetServer);
		model.addAttribute(CRUDController.LIST_OB_NAME, dumpFolders);
		return "mysql-restore-list";
	}

	@Override
	public List<MainMenuItem> getMenuItems() {
		return null;
	}

}
