package com.go2wheel.mysqlbackup.service;


import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.model.BackupFolder;
import com.go2wheel.mysqlbackup.model.BackupFolderState;
import com.go2wheel.mysqlbackup.model.Server;

public class TestBackupFolderStateService extends ServiceTbase {
	
	
	
	@Autowired
	private ServerService sservice;
	
	@Autowired
	private BackupFolderService bfervice;
	
	@Autowired
	private BackupFolderStateService bfstervice;

	
	
	@Before
	public void b() {
		deleteAll(bfstervice);
		deleteAll(bfervice);
		deleteAll(sservice);
	}
	
	private BackupFolderState createOne(String folderName) {
		Server server = new Server(serverHost, "a server.");
		server = sservice.save(server);
		BackupFolder bf = new BackupFolder(server.getId(), folderName);
		bf = bfervice.save(bf);
		BackupFolderState bfs = new BackupFolderState();
		bfs.setBackupFolderId(bf.getId());
		bfs.setHowMany(5);
		bfs.setTotalSizeInKb(55);
		return bfstervice.save(bfs);
	}
	
	@Test
	public void t() {
		BackupFolderState bfs = createOne("/etc");
		assertThat(bfs.getId(), greaterThan(99));
		BackupFolder bf = bfervice.findByServerHostAndFolder(serverHost, "/etc");
		assertThat(bfs.getBackupFolderId(), equalTo(bf.getId()));
		assertNotNull(bfs.getCreatedAt());
	}

}
