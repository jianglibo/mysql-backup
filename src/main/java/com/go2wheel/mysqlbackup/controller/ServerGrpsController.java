package com.go2wheel.mysqlbackup.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.go2wheel.mysqlbackup.model.ServerGrp;
import com.go2wheel.mysqlbackup.service.ServerGrpDbService;


@Controller
@RequestMapping(ServerGrpsController.MAPPING_PATH)
public class ServerGrpsController  extends CRUDController<ServerGrp, ServerGrpDbService> {
	
	protected static final String MAPPING_PATH = "/app/server-grps"; 
	
	@Autowired
	public ServerGrpsController(ServerGrpDbService dService) {
		super(ServerGrp.class, dService, MAPPING_PATH, "服务器组");
	}
	
	@Override
	public ServerGrp newModel() {
		return new ServerGrp();
	}

	@Override
	void copyProperties(ServerGrp entityFromForm, ServerGrp entityFromDb) {
		entityFromDb.setEname(entityFromForm.getEname());
		entityFromDb.setMsgkey(entityFromForm.getMsgkey());
	}

}
