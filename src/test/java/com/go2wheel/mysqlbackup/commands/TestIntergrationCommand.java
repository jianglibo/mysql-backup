package com.go2wheel.mysqlbackup.commands;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.go2wheel.mysqlbackup.value.ExecuteResult;
import com.go2wheel.mysqlbackup.value.MysqlInstance;

@SpringBootTest("spring.shell.interactive.enabled=false")
@RunWith(SpringRunner.class)
public class TestIntergrationCommand {
	
	@Autowired
	private BackupCommand backupCommand;
	
	@Test
	public void t() {
		assertTrue(true);
	}
	
//	@Test
//	public void t() {
//		ExecuteResult<MysqlInstance> er = backupCommand.createInstance("a", 2,0, "", "", "");
//		assertFalse(er.isSuccess());
//	}

}
