package com.go2wheel.mysqlbackup.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.go2wheel.mysqlbackup.model.Subscribe;
import com.go2wheel.mysqlbackup.service.ReuseableCronDbService;
import com.go2wheel.mysqlbackup.service.ServerGrpDbService;
import com.go2wheel.mysqlbackup.service.SubscribeDbService;
import com.go2wheel.mysqlbackup.service.UserAccountDbService;
import com.go2wheel.mysqlbackup.valueprovider.TemplateValueProvider;


@Controller
@RequestMapping(SubscribesController.MAPPING_PATH)
public class SubscribesController  extends CRUDController<Subscribe, SubscribeDbService> {
	
	public static final String MAPPING_PATH = "/app/subscribes";
	
	@Autowired
	private ReuseableCronDbService reuseableCronDbService;

	@Autowired
	private UserAccountDbService userAccountDbService;
	
	@Autowired
	private ServerGrpDbService serverGrpDbService;
	
	@Autowired
	private TemplateValueProvider tvp;
	
	@Autowired
	public SubscribesController(SubscribeDbService dbService) {
		super(Subscribe.class, dbService, MAPPING_PATH);
	}

	@Override
	boolean copyProperties(Subscribe entityFromForm, Subscribe entityFromDb) {
		entityFromDb.setName(entityFromForm.getName());
		entityFromDb.setCronExpression(entityFromForm.getCronExpression());
		entityFromDb.setServerGrpId(entityFromForm.getServerGrpId());
		entityFromDb.setTemplate(entityFromForm.getTemplate());
		entityFromDb.setUserAccountId(entityFromForm.getUserAccountId());
		return true;
	}

	@Override
	public Subscribe newModel() {
		return new Subscribe();
	}

	@Override
	protected void formAttribute(Model model) {
		model.addAttribute("crons", reuseableCronDbService.findAll());
		model.addAttribute("userAccounts", userAccountDbService.findAll());
		model.addAttribute("serverGroups", serverGrpDbService.findAll());
		model.addAttribute("templates", tvp.findTopTemplates(""));
	}
	
	@Override
	protected String deleteEntities(List<Subscribe> entities, boolean execute) {
		return super.deleteEntities(entities, true);
	}
	
	@Override
	protected void listExtraAttributes(Model model) {
	}
	
	@Override
	protected int getMenuOrder() {
		return 300;
	}
}
