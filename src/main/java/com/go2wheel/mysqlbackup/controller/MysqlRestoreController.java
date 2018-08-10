package com.go2wheel.mysqlbackup.controller;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.go2wheel.mysqlbackup.commands.MysqlService;
import com.go2wheel.mysqlbackup.exception.AppNotStartedException;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.exception.ScpException;
import com.go2wheel.mysqlbackup.exception.UnExpectedContentException;
import com.go2wheel.mysqlbackup.model.PlayBack;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.ui.MainMenuItem;
import com.go2wheel.mysqlbackup.value.AsyncTaskValue;
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
	public String listLocalDumps(@PathVariable PlayBack playback, Model model, HttpServletRequest request)
			throws IOException {
		Server sourceServer = serverDbService.findById(playback.getSourceServerId());
		Server targetServer = serverDbService.findById(playback.getTargetServerId());
		List<MysqlDumpFolder> dumpFolders = mysqlService.listDumpFolders(sourceServer);
		model.addAttribute("sourceServer", sourceServer);
		model.addAttribute("targetServer", targetServer);
		model.addAttribute(CRUDController.LIST_OB_NAME, dumpFolders);
		return "mysql-restore-list";
	}

	@PostMapping("/{playback}")
	public String playback(@PathVariable PlayBack playback,@RequestParam(name="dump") String dumpFolder, Model model, HttpServletRequest request, RedirectAttributes ras) throws IOException, RunRemoteCommandException, UnExpectedContentException, JSchException, AppNotStartedException, ScpException {
		Server sourceServer = serverDbService.findById(playback.getSourceServerId());
		Server targetServer = serverDbService.findById(playback.getTargetServerId());
		
		String msgkey = messageSource.getMessage("taskkey.restoremysql", new Object[] {sourceServer.getId(), targetServer.getId()}, request.getLocale());
		
		CompletableFuture<AsyncTaskValue> cf = mysqlService.restoreAsync(playback, sourceServer, targetServer, dumpFolder, msgkey);
		
		String sid = request.getSession(true).getId();
		globalStore.saveAfuture(sid, msgkey, cf);
		
		ras.addFlashAttribute("formProcessSuccessed", "任务已异步发送，稍后会通知您。");
		ServletUriComponentsBuilder ucb = ServletUriComponentsBuilder.fromRequest(request);
		
		String uri = ucb.build().toUriString();
		return "redirect:" + uri;
	}

	@Override
	public List<MainMenuItem> getMenuItems() {
		return null;
	}

}
