package com.go2wheel.mysqlbackup.expect;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Optional;

import org.junit.Test;

import com.go2wheel.mysqlbackup.jsch.SshBaseFort;
import com.go2wheel.mysqlbackup.util.MysqlDumpExpect;
import com.go2wheel.mysqlbackup.util.ScpUtil;
import com.go2wheel.mysqlbackup.util.StringUtil.LinuxLsl;
import com.jcraft.jsch.JSchException;

public class TestMysqlDumpExpect extends SshBaseFort {
	
	@Test
	public void t() throws JSchException, IOException {
		MysqlDumpExpect mde = new MysqlDumpExpect(session, box);
		Optional<LinuxLsl> result = mde.start();
		assertTrue(result.isPresent());
		
		int i = ScpUtil.from(session, MysqlDumpExpect.DUMP_FILE).size();
		assertThat((long)i, equalTo(result.get().getSize()));
	
	}

}
