package com.go2wheel.mysqlbackup.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.DiskfreeRecord;
import com.go2wheel.mysqlbackup.model.Diskfree;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.repository.DiskfreeRepository;
import com.go2wheel.mysqlbackup.value.Box;

@Service
@Validated
public class DiskfreeService extends ServiceBase<DiskfreeRecord, Diskfree> {
	
	@Autowired
	private ServerService serverService;

	public DiskfreeService(DiskfreeRepository repo) {
		super(repo);
	}

	public List<Diskfree> getItemsInDays(Box box, int days) {
		Server server = serverService.findByHost(box.getHost());
		return  ((DiskfreeRepository)repo).getItemsInDays(server.getId(), days);
	}
	
}
