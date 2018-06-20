package com.go2wheel.mysqlbackup.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.BorgDownloadRecord;
import com.go2wheel.mysqlbackup.model.BorgDownload;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.repository.BorgDownloadRepository;

@Service
@Validated
public class BorgDownloadDbService extends DbServiceBase<BorgDownloadRecord, BorgDownload> {
	
	@Autowired
	private ServerDbService serverDbService;

	public BorgDownloadDbService(BorgDownloadRepository repo) {
		super(repo);
	}

	public List<BorgDownload> getItemsInDays(Server box, int days) {
		Server sv = serverDbService.findByHost(box.getHost());
		return ((BorgDownloadRepository)repo).getItemsInDays(sv.getId(), days);
	}

	
}
