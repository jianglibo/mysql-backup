package com.go2wheel.mysqlbackup.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.UserAccountRecord;
import com.go2wheel.mysqlbackup.model.UserAccount;
import com.go2wheel.mysqlbackup.repository.UserAccountRepository;

@Service
@Validated
public class UserAccountService extends ServiceBase<UserAccountRecord, UserAccount> {

	@Autowired
	public UserAccountService(UserAccountRepository serverRepository) {
		super(serverRepository);
	}
	
	public UserAccount findByEmail(String email) {
		return ((UserAccountRepository)repo).findByEmail(email);
	}
	
	public UserAccount findByMobile(String mobile) {
		return ((UserAccountRepository)repo).findByMobile(mobile);
	}

	public UserAccount findByName(String name) {
		return ((UserAccountRepository)repo).findByName(name);
	}

	public List<UserAccount> findLikeName(String partOfName) {
		return ((UserAccountRepository)repo).findLikeName(partOfName);
	}
}
