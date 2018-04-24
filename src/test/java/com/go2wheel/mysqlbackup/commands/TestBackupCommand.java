package com.go2wheel.mysqlbackup.commands;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.go2wheel.mysqlbackup.UtilForTe;
import com.go2wheel.mysqlbackup.value.ExecuteResult;
import com.go2wheel.mysqlbackup.value.MysqlInstance;
import com.go2wheel.mysqlbackup.yml.YamlInstance;

public class TestBackupCommand {
	
	private BackupCommand bc;
	
	@Before
	public void before() throws IOException {
		bc = UtilForTe.backupCommandInstance();
	}
	
	@After
	public void after()  {
		Path p = bc.getInstancesBase();
		bc.setInstancesBase(null);
		try {
			UtilForTe.deleteFolder(p);
		} catch (IOException e) {
		}
	}
	
	
	
//	@Test
//	public void testCreateInstance() throws IOException {
//		ExecuteResult<MysqlInstance> er = bc.createInstance("localhost", 0, 0, "", "root", "123");
//		assertTrue(er.isSuccess());
//		assertTrue("host directory should be created.", Files.exists(bc.getInstancesBase().resolve(er.getResult().getHost())));
//		Path df = bc.getInstancesBase().resolve(er.getResult().getHost()).resolve(BackupCommand.DESCRIPTION_FILENAME);
//		MysqlInstance miCreated = er.getResult();
//		MysqlInstance miLoaded = YamlInstance.INSTANCE.getYaml().loadAs(Files.newInputStream(df), MysqlInstance.class);
//		assertThat(miCreated.getPassword(), equalTo(miLoaded.getPassword()));
//		assertThat(miCreated.getSshPort(), equalTo(miLoaded.getSshPort()));
//		
//		assertThat(miLoaded.getSshPort(), equalTo(22));
//		assertThat(miLoaded.getMysqlPort(), equalTo(3306));
//	}

}
