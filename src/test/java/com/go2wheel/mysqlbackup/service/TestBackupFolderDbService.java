package com.go2wheel.mysqlbackup.service;


import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import com.go2wheel.mysqlbackup.model.BackupFolder;
import com.go2wheel.mysqlbackup.model.Server;

public class TestBackupFolderDbService extends ServiceTbase {
	
	@Autowired
	private ServerDbService sservice;
	
	@Autowired
	private BackupFolderDbService service;
	
	@Before
	public void b() {
		deleteAll(service);
		deleteAll(sservice);
	}
	
	private BackupFolder createOne(String folderName) {
		Server server = new Server(HOST_DEFAULT, "a server");
		server = sservice.save(server);
		
		BackupFolder bf = new BackupFolder(server.getId(), folderName);
		return service.save(bf);
	}
	
	@Test
	public void t() {
		BackupFolder bf = createOne("/etc");
		assertThat(bf.getId(), greaterThan(99));
		Server sv = sservice.findByHost(HOST_DEFAULT);
		assertThat(bf.getServerId(), equalTo(sv.getId()));
	}
	
	@Test(expected = DataIntegrityViolationException.class)
	public void deleteServer() {
//		BackupFolder bf = createOne("/etc");
		sservice.findAll().forEach(sservice::delete);
	}

}
