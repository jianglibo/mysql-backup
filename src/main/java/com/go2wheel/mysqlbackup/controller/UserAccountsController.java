package com.go2wheel.mysqlbackup.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RequestMapping;

import com.go2wheel.mysqlbackup.dbservice.UserAccountDbService;
import com.go2wheel.mysqlbackup.model.UserAccount;


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
	protected void parseDuplicateKeyException(DuplicateKeyException de, String unique, BindingResult bindingResult) {
		String fn = null;
		String uUpcase = unique.toUpperCase();
		if (uUpcase.contains("NAME")) {
			fn = "name";
		}else if (uUpcase.contains("EMAIL")) {
			fn = "email";
		} else if(uUpcase.contains("MOBILE")) {
			fn = "mobile";
		}
		
		if (fn == null) {
			super.parseDuplicateKeyException(de, unique, bindingResult);
		} else {
			FieldError fe = new FieldError(OB_NAME, fn, getLowerHyphenClassName(de.getClass()));
			bindingResult.addError(fe);
		}
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
