package com.go2wheel.mysqlbackup.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.StorageStateRecord;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.StorageState;
import com.go2wheel.mysqlbackup.repository.StorageStateRepository;

@Service
@Validated
public class StorageStateDbService extends ServiceBase<StorageStateRecord, StorageState> {
	
	@Autowired
	private ServerDbService serverDbService;

	public StorageStateDbService(StorageStateRepository repo) {
		super(repo);
	}

	public List<StorageState> getItemsInDays(Server box, int days) {
		Server server = serverDbService.findByHost(box.getHost());
		return  ((StorageStateRepository)repo).getItemsInDays(server.getId(), days);
	}

	public int remove(Server server, int keepDays) {
		return  ((StorageStateRepository)repo).removeBeforeDay(server, keepDays);
	}

	
	
}
