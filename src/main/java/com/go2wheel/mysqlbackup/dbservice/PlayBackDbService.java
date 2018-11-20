package com.go2wheel.mysqlbackup.dbservice;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.PlayBackRecord;
import com.go2wheel.mysqlbackup.model.PlayBack;
import com.go2wheel.mysqlbackup.repository.PlayBackRepository;

@Service
@Validated
public class PlayBackDbService extends DbServiceBase<PlayBackRecord, PlayBack> {
	
	public PlayBackDbService(PlayBackRepository repo) {
		super(repo);
	}

}
