package com.go2wheel.mysqlbackup.dbservice;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.SubscribeRecord;
import com.go2wheel.mysqlbackup.model.ServerGrp;
import com.go2wheel.mysqlbackup.model.UserAccount;
import com.go2wheel.mysqlbackup.model.Subscribe;
import com.go2wheel.mysqlbackup.repository.SubscribeRepository;

@Service
@Validated
public class SubscribeDbService extends DbServiceBase<SubscribeRecord, Subscribe> {

	@Autowired
	public SubscribeDbService(SubscribeRepository userServerGrpRepository) {
		super(userServerGrpRepository);
	}

	public List<Subscribe> findByUser(UserAccount ua) {
		return ((SubscribeRepository)repo).findByUser(ua);
	}

	public Subscribe findByUserAndServerGrp(@NotNull UserAccount user, ServerGrp serverGroup) {
		return ((SubscribeRepository)repo).findByUserAndServerGrp(user, serverGroup);
	}


}
