package com.go2wheel.mysqlbackup.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.go2wheel.mysqlbackup.model.UserAccount;
import com.go2wheel.mysqlbackup.service.UserAccountDbService;


@Controller
@RequestMapping(UserAccountsController.MAPPING_PATH)
public class UserAccountsController  extends CRUDController<UserAccount, UserAccountDbService> {
	
	public static final String MAPPING_PATH = "/app/user-accounts";
	
	@Autowired
	public UserAccountsController(UserAccountDbService dbService) {
		super(UserAccount.class, dbService, MAPPING_PATH);
	}

	@Override
	boolean copyProperties(UserAccount entityFromForm, UserAccount entityFromDb) {
		entityFromDb.setEmail(entityFromForm.getEmail());
		entityFromDb.setMobile(entityFromForm.getMobile());
		entityFromDb.setName(entityFromForm.getName());
		return true;
	}

	@Override
	public UserAccount newModel() {
		return new UserAccount();
	}

	@Override
	protected void formAttribute(Model model) {
		
	}

	@Override
	protected void listExtraAttributes(Model model) {
	}
	
	@Override
	protected int getMenuOrder() {
		return 400;
	}
}
