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
	void copyProperties(Server entityFromForm, Server entityFromDb) {
		entityFromDb.setName(entityFromForm.getName());
		entityFromDb.setHost(entityFromForm.getHost());
		entityFromDb.setUsername(entityFromForm.getUsername());
		entityFromDb.setPassword(entityFromForm.getPassword());
		entityFromDb.setServerStateCron(entityFromForm.getServerStateCron());
		entityFromDb.setStorageStateCron(entityFromForm.getStorageStateCron());
		entityFromDb.setSshKeyFile(entityFromForm.getSshKeyFile());
		entityFromDb.setServerRole(entityFromForm.getServerRole());
		entityFromDb.setOs(entityFromForm.getOs());
	}

	@Override
	public Server newModel() {
		return new Server();
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
	
//	@GetMapping("")
//	String getPage(Model model) {
//		List<Server> servers = serverDbService.findAll();
//		model.addAttribute(LIST_OB_NAME, servers);
//		return LIST_TPL;
//	}
//	
//	@GetMapping("/create")
//	String getCreate(Model model) {
//		model.addAttribute(OB_NAME, new Server());
//		model.addAttribute("oses", getOses());
//		return FORM_TPL;
//	}
//	
//	@PostMapping("/create")
//	String postCreate(@Validated @ModelAttribute(OB_NAME) Server server, final BindingResult bindingResult,Model model, RedirectAttributes ras) {
//	    if (bindingResult.hasErrors()) {
//	    	model.addAttribute("oses", getOses());
//	        return FORM_TPL;
//		}
//		
//		serverDbService.save(server);
//	    
//	    ras.addFlashAttribute("formProcessSuccessed", true);
//	    return "redirect:" + uri;
//	}
//
//	@GetMapping("/{id}/edit")
//	String getEdit(@PathVariable(name="id") Server server, Model model) {
//		model.addAttribute(OB_NAME, server);
//		model.addAttribute("editing", true);
//		model.addAttribute("oses", getOses());
//		model.addAttribute("crons", reuseableCronDbService.findAll());
//		return FORM_TPL;
//	}
//	
//	@PutMapping("/{id}/edit")
//	String putEdit(@Validated @ModelAttribute(OB_NAME) Server serverUpdated, @PathVariable(name="id") Server serverOrigin,  final BindingResult bindingResult,Model model, RedirectAttributes ras) {
//		if (bindingResult.hasErrors()) {
//	        return FORM_TPL;
//		}
//		serverOrigin.setName(serverUpdated.getName());
//		serverOrigin.setHost(serverUpdated.getHost());
//		
//		serverOrigin.setUsername(serverUpdated.getUsername());
//		serverOrigin.setPassword(serverUpdated.getPassword());
//		serverOrigin.setServerStateCron(serverUpdated.getServerStateCron());
//		serverOrigin.setStorageStateCron(serverUpdated.getStorageStateCron());
//		serverOrigin.setSshKeyFile(serverUpdated.getSshKeyFile());
//		serverOrigin.setServerRole(serverUpdated.getServerRole());
//		serverOrigin.setOs(serverUpdated.getOs());
//		serverDbService.save(serverOrigin);
//        ras.addFlashAttribute("formProcessSuccessed", true);
//	    return "redirect:" + uri;
//	}
//	@Override
//	public List<MainMenuItem> getMenuItems() {
//		return Arrays.asList(new MainMenuItem("appmodel", "servers", uri, 100));
//	}
}
