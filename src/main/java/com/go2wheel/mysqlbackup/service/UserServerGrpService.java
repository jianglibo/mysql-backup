package com.go2wheel.mysqlbackup.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.UserAndServerGrpRecord;
import com.go2wheel.mysqlbackup.model.UserAccount;
import com.go2wheel.mysqlbackup.model.UserServerGrp;
import com.go2wheel.mysqlbackup.repository.UserServerGrpRepository;

@Service
@Validated
public class UserServerGrpService extends ServiceBase<UserAndServerGrpRecord, UserServerGrp> {

	@Autowired
	public UserServerGrpService(UserServerGrpRepository userServerGrpRepository) {
		super(userServerGrpRepository);
	}

	public List<UserServerGrp> findByUser(UserAccount ua) {
		return ((UserServerGrpRepository)repo).findByUser(ua);
	}

}
