package com.go2wheel.mysqlbackup.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.go2wheel.mysqlbackup.model.MysqlInstance;
import com.go2wheel.mysqlbackup.service.MysqlInstanceDbService;
import com.go2wheel.mysqlbackup.service.ReuseableCronDbService;


@Controller
@RequestMapping(MysqlInstancesController.MAPPING_PATH)
public class MysqlInstancesController  extends CRUDController<MysqlInstance, MysqlInstanceDbService> {
	
	public static final String MAPPING_PATH = "/app/mysql-instances";
	
	@Autowired
	private ReuseableCronDbService reuseableCronDbService;
	
	@Autowired
	public MysqlInstancesController(MysqlInstanceDbService dbService) {
		super(MysqlInstance.class, dbService, MAPPING_PATH);
	}

	@Override
	boolean copyProperties(MysqlInstance entityFromForm, MysqlInstance entityFromDb) {
		entityFromDb.setHost(entityFromForm.getHost());
		entityFromDb.setPort(entityFromForm.getPort());
		entityFromDb.setUsername(entityFromForm.getUsername());
		entityFromDb.setPassword(entityFromForm.getPassword());
		entityFromDb.setDumpFileName(entityFromDb.getDumpFileName());
		entityFromDb.setClientBin(entityFromForm.getClientBin());
		entityFromDb.setFlushLogCron(entityFromForm.getFlushLogCron());
		return true;
	}
	
	@GetMapping("/create")
	@Override
	String getCreate(Model model, HttpServletRequest httpRequest) {
		String serverId = httpRequest.getParameter("server");
		if (serverId == null) {
			return redirectMappingUrl();
		}
		MysqlInstance mi = newModel();
		mi.setServerId(Integer.parseInt(serverId));
		model.addAttribute(OB_NAME, mi);
		model.addAttribute("editting", false);
		commonAttribute(model);
		formAttribute(model);
		return getFormTpl();
	}

	@Override
	public MysqlInstance newModel() {
		return new MysqlInstance.MysqlInstanceBuilder(0, "").build();
	}

	@Override
	protected void formAttribute(Model model) {
		model.addAttribute("crons", reuseableCronDbService.findAll());
	}

	@Override
	protected void listExtraAttributes(Model model) {
	}
	
	protected int getMenuOrder() {
		return 600;
	}
}
