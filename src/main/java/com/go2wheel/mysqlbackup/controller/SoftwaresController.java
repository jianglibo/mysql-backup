package com.go2wheel.mysqlbackup.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.go2wheel.mysqlbackup.dbservice.SoftwareDbService;
import com.go2wheel.mysqlbackup.model.Software;


@Controller
@RequestMapping(SoftwaresController.MAPPING_PATH)
public class SoftwaresController  extends CRUDController<Software, SoftwareDbService> {
	
	public static final String MAPPING_PATH = "/app/softwares";
	
	@Autowired
	public SoftwaresController(SoftwareDbService dbService) {
		super(Software.class, dbService, MAPPING_PATH);
	}

	@Override
	boolean copyProperties(Software entityFromForm, Software entityFromDb) {
		return false;
	}

	@Override
	public Software newModel() {
		return new Software();
	}
	
	
	@Override
	protected String deleteEntities(List<Software> entities, boolean execute) {
		return super.deleteEntities(entities, true);
	}

	@Override
	protected void formAttribute(Model model) {
		
	}

	@Override
	protected void listExtraAttributes(Model model) {
	}
	
	protected int getMenuOrder() {
		return 1000;
	}
}
