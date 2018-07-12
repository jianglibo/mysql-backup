package com.go2wheel.mysqlbackup.controller;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.service.ReuseableCronDbService;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.valueprovider.SharedValueProviderMethods;
import com.google.common.collect.Sets;


@Controller
@RequestMapping(ServersController.MAPPING_PATH)
public class ServersController  extends  CRUDController<Server, ServerDbService> {
	
	@Autowired
	public ServersController(ServerDbService dbService) {
		super(Server.class, dbService, MAPPING_PATH);
	}
	public static final String MAPPING_PATH = "/app/servers";

	@Autowired
	private ReuseableCronDbService reuseableCronDbService;

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
		return true;
	}

	@Override
	public Server newModel() {
		return new Server();
	}
	
	@Override
	protected int getMenuOrder() {
		return 100;
	}
	
	
	private Set<String> getOses() {
		List<String> oses = getDbService().findDistinctOsType("");
		Set<String> orderedUnique = Sets.newTreeSet();
		orderedUnique.addAll(oses);
		orderedUnique.addAll(SharedValueProviderMethods.predefines);
		return orderedUnique;
	}

	@Override
	protected void formAttribute(Model model) {
		model.addAttribute("oses", getOses());
		model.addAttribute("crons", reuseableCronDbService.findAll());
	}

	@Override
	protected void listExtraAttributes(Model model) {
	}

}
