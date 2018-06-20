package com.go2wheel.mysqlbackup.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.go2wheel.mysqlbackup.jooqschema.tables.records.DiskfreeRecord;
import com.go2wheel.mysqlbackup.model.Diskfree;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.repository.DiskfreeRepository;

@Service
@Validated
public class DiskfreeDbService extends DbServiceBase<DiskfreeRecord, Diskfree> {
	
	@Autowired
	private ServerDbService serverDbService;

	public DiskfreeDbService(DiskfreeRepository repo) {
		super(repo);
	}

	public List<Diskfree> getItemsInDays(Server box, int days) {
		Server server = serverDbService.findByHost(box.getHost());
		return  ((DiskfreeRepository)repo).getItemsInDays(server.getId(), days);
	}
	
}
