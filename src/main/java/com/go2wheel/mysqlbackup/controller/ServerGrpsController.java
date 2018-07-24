package com.go2wheel.mysqlbackup.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.ServerGrp;
import com.go2wheel.mysqlbackup.service.ServerDbService;
import com.go2wheel.mysqlbackup.service.ServerGrpDbService;
import com.go2wheel.mysqlbackup.value.IdBinder;
import com.go2wheel.mysqlbackup.value.RedirectAjaxBody;


@Controller
@RequestMapping(ServerGrpsController.MAPPING_PATH)
public class ServerGrpsController  extends CRUDController<ServerGrp, ServerGrpDbService> {
	
	protected static final String MAPPING_PATH = "/app/server-grps"; 
	
	@Autowired
	private ServerDbService serverDbService;
	
    @ExceptionHandler
    public ResponseEntity<String> handle(Exception ex) {
    	ex.printStackTrace();
    	return ResponseEntity.ok("hello");
    }
	
	@Autowired
	public ServerGrpsController(ServerGrpDbService dService) {
		super(ServerGrp.class, dService, MAPPING_PATH);
	}
	
	
	@PostMapping("/{id}/servers")
	public String addServer(@PathVariable(name="id") ServerGrp serverGrp,@RequestParam(name="serverId") Server server, ServletUriComponentsBuilder servletUriComponentsBuilder) {
		getDbService().addServer(serverGrp, server);
		String uri = servletUriComponentsBuilder.replacePath(ServersController.MAPPING_PATH).replaceQuery(null).queryParam("server-grp", serverGrp.getId()).build().toUriString();
		return "redirect:" + uri;
	}

//	@DeleteMapping("/{id}/servers")
////	public String remove(@PathVariable(name="id") ServerGrp serverGrp,@RequestParam(name="server") Server server, ServletUriComponentsBuilder servletUriComponentsBuilder) {
//	public String remove(@PathVariable(name="id") ServerGrp serverGrp,@RequestBody Map<String, Object> server, ServletUriComponentsBuilder servletUriComponentsBuilder) {
////		getDbService().removeServer(serverGrp, server);
//		String uri = servletUriComponentsBuilder.replacePath(ServersController.MAPPING_PATH).replaceQuery(null).queryParam("server-grp", serverGrp.getId()).build().toUriString();
//		return "redirect:" + uri;
//	}
	// default Html form submit: application/x-www-form-urlencoded
	// requestBody is for application/json.
	// 
	@DeleteMapping("/{id}/servers")
	@ResponseBody
	public RedirectAjaxBody  remove(@PathVariable(name="id") ServerGrp serverGrp, @ModelAttribute IdBinder idBinder, ServletUriComponentsBuilder servletUriComponentsBuilder) {
		Server server = serverDbService.findById(idBinder.getId());
		getDbService().removeServer(serverGrp, server);
		String uri = servletUriComponentsBuilder.replacePath(ServersController.MAPPING_PATH).replaceQuery(null).queryParam("server-grp", serverGrp.getId()).build().toUriString();
		return new RedirectAjaxBody(uri);
	}
	
	@Override
	public ServerGrp newModel() {
		return new ServerGrp();
	}

	@Override
	boolean copyProperties(ServerGrp entityFromForm, ServerGrp entityFromDb) {
		entityFromDb.setEname(entityFromForm.getEname());
		entityFromDb.setMsgkey(entityFromForm.getMsgkey());
		return true;
	}

	@Override
	protected void formAttribute(Model model) {
	}

	@Override
	protected void listExtraAttributes(Model model) {
	}


	@Override
	protected int getMenuOrder() {
		return 200;
	}

}
