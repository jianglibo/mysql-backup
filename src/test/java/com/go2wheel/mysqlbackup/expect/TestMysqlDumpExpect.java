package com.go2wheel.mysqlbackup.expect;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.util.List;

import org.junit.Test;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.util.Md5Checksum;
import com.go2wheel.mysqlbackup.util.MysqlUtil;
import com.go2wheel.mysqlbackup.util.ScpUtil;
import com.go2wheel.mysqlbackup.value.LinuxLsl;

public class TestMysqlDumpExpect extends SpringBaseFort {
	
	@Test
	public void t() throws Exception {
		createALocalFile(" ");
		MysqlDumpExpect mde = new MysqlDumpExpect(session, server);
		List<String> result = mde.start();
		assertTrue(result.size() == 2);
		ScpUtil.from(session, MysqlUtil.DUMP_FILE_NAME, tmpFile.toAbsolutePath().toString());
		
		LinuxLsl llsl = LinuxLsl.matchAndReturnLinuxLsl(result).get();
		
		assertThat(Files.size(tmpFile), equalTo(llsl.getSize()));
		
		String md5 = Md5Checksum.getMD5Checksum(tmpFile.toString());
		
		assertThat(md5, equalTo(llsl.getMd5()));
	
	}

}
