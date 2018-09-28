package com.go2wheel.mysqlbackup.controller;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.go2wheel.mysqlbackup.SettingsInDb;
import com.go2wheel.mysqlbackup.job.ServerStateJob;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.ServerGrp;
import com.go2wheel.mysqlbackup.model.Software;
import com.go2wheel.mysqlbackup.service.ReusableCronDbService;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.service.ServerGrpDbService;
import com.go2wheel.mysqlbackup.value.AjaxDataResult;
import com.go2wheel.mysqlbackup.value.AjaxResult;
import com.jcraft.jsch.JSchException;


@Controller
@RequestMapping(ServersController.MAPPING_PATH)
public class ServersController  extends  CRUDController<Server, ServerDbService> {
	
	@Autowired
	public ServersController(ServerDbService dbService) {
		super(Server.class, dbService, MAPPING_PATH);
	}
	public static final String MAPPING_PATH = "/app/servers";

	@Autowired
	private ReusableCronDbService reuseableCronDbService;
	
	@Autowired
	private ServerGrpDbService serverGrpDbService;
	
	@Autowired
	private SettingsInDb settingsInDb;
	
	@Autowired
	private ServerStateJob serverStateJob;

	@Override
	boolean copyProperties(Server entityFromForm, Server entityFromDb) {
		entityFromDb.setName(entityFromForm.getName());
		entityFromDb.setHost(entityFromForm.getHost());
		entityFromDb.setUsername(entityFromForm.getUsername());
		entityFromDb.setPassword(entityFromForm.getPassword());
		entityFromDb.setServerStateCron(entityFromForm.getServerStateCron());
		entityFromDb.setStorageStateCron(entityFromForm.getStorageStateCron());
		entityFromDb.setSshKeyFile(entityFromForm.getSshKeyFile());
		entityFromDb.setServerRole(entityFromForm.getServerRole());
		entityFromDb.setOs(entityFromForm.getOs());
		entityFromDb.setCoreNumber(entityFromForm.getCoreNumber());
		return true;
	}
	
	@PutMapping("/{server}/update-info")
	public ResponseEntity<AjaxResult> updateServerData(@PathVariable(name="server") Server server, Model model) throws JSchException, IOException {
		serverStateJob.lockWrapped(server, "fromcontroller");
		return ResponseEntity.ok(new AjaxDataResult<Server>(server));
	}

	@Override
	public Server newModel() {
		return new Server();
	}
	
	@Override
	protected int getMenuOrder() {
		return 100;
	}
	
	@Override
	String getListPage(Model model, HttpServletRequest request) {
		String serverGrpId = request.getParameter("server-grp");
		if (serverGrpId == null) {
			return super.getListPage(model, request);
		} else {
			ServerGrp serverGrp = serverGrpDbService.findById(serverGrpId);
			List<Server> grpServers = serverGrpDbService.getServers(serverGrp);
			model.addAttribute(LIST_OB_NAME, grpServers);
			List<Server> otherServers = getDbService().findAll().stream().filter(s -> 
				!(grpServers.stream().anyMatch(ss -> ss.getId().equals(s.getId())))
				).collect(Collectors.toList());
			model.addAttribute("otherServers", otherServers);
			model.addAttribute("serverGrp", serverGrp);
			commonAttribute(model);
			listExtraAttributes(model);
			return getListTpl();
		}
	}
	
	@PostMapping("/{server}/install")
	public String installSoftware(@PathVariable Server server,HttpServletRequest request, @RequestParam Software software) {
		
		return redirectListingUrl(request);
	}

	@Override
	protected void formAttribute(Model model) {
		model.addAttribute("oses", settingsInDb.getListString(SettingsInDb.OSTYPE_PREFIX));
		model.addAttribute("crons", reuseableCronDbService.findAll());
	}

	@Override
	protected void listExtraAttributes(Model model) {
	}

}
