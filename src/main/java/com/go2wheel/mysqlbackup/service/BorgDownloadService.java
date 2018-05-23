package com.go2wheel.mysqlbackup.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.BorgDownloadRecord;
import com.go2wheel.mysqlbackup.model.BorgDownload;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.repository.BorgDownloadRepository;
import com.go2wheel.mysqlbackup.value.Box;

@Service
@Validated
public class BorgDownloadService extends ServiceBase<BorgDownloadRecord, BorgDownload> {
	
	@Autowired
	private ServerService serverService;

	public BorgDownloadService(BorgDownloadRepository repo) {
		super(repo);
	}

	public List<BorgDownload> getItemsInDays(Box box, int days) {
		Server sv = serverService.findByHost(box.getHost());
		return ((BorgDownloadRepository)repo).getItemsInDays(sv.getId(), days);
	}

	
}
