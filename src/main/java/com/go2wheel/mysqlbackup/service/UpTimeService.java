package com.go2wheel.mysqlbackup.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.UpTimeRecord;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.UpTime;
import com.go2wheel.mysqlbackup.repository.UpTimeRepository;

@Service
@Validated
public class UpTimeService extends ServiceBase<UpTimeRecord, UpTime> {
	
	@Autowired
	private ServerService serverService;

	public UpTimeService(UpTimeRepository repo) {
		super(repo);
	}

	public List<UpTime> getItemsInDays(Server box, int days) {
		Server server = serverService.findByHost(box.getHost());
		return  ((UpTimeRepository)repo).getItemsInDays(server.getId(), days);
	}

	
	
}
