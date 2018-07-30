package com.go2wheel.mysqlbackup.service;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.PlayBackResultRecord;
import com.go2wheel.mysqlbackup.model.PlayBackResult;
import com.go2wheel.mysqlbackup.repository.PlayBackResultRepository;

@Service
@Validated
public class PlayBackResultDbService extends DbServiceBase<PlayBackResultRecord, PlayBackResult> {

	public PlayBackResultDbService(PlayBackResultRepository repo) {
		super(repo);
	}
}
