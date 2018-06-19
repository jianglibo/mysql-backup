package com.go2wheel.mysqlbackup.service;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.PlayBackRecord;
import com.go2wheel.mysqlbackup.model.PlayBack;
import com.go2wheel.mysqlbackup.repository.PlayBackRepository;

@Service
@Validated
public class PlayBackDbService extends ServiceBase<PlayBackRecord, PlayBack> {
	
	public PlayBackDbService(PlayBackRepository repo) {
		super(repo);
	}

}
