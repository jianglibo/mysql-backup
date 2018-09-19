package com.go2wheel.mysqlbackup.expect;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.model.MysqlInstance;
import com.go2wheel.mysqlbackup.util.Md5Checksum;
import com.go2wheel.mysqlbackup.util.ScpUtil;
import com.go2wheel.mysqlbackup.value.LinuxLsl;

public class TestMysqlDumpExpect extends SpringBaseFort {
	
    @Rule
    public TemporaryFolder tfolder= new TemporaryFolder();	
	
	@Test
	public void t() throws Exception {
		clearDb();
		createSession();
		createMysqlIntance();
		Path tmpFile = createALocalFile(tfolder.newFile().toPath(), " ");
		MysqlDumpExpect mde = new MysqlDumpExpect(session, server);
		List<String> result = mde.start();
		assertTrue(result.size() == 2);
		ScpUtil.from(session, MysqlInstance.getDefaultDumpFileName(server.getOs()), tmpFile.toAbsolutePath().toString());
		
		LinuxLsl llsl = LinuxLsl.matchAndReturnLinuxLsl(result).get();
		
		assertThat(Files.size(tmpFile), equalTo(llsl.getSize()));
		
		String md5 = Md5Checksum.getMD5Checksum(tmpFile.toString());
		
		assertThat(md5, equalTo(llsl.getMd5()));
	
	}

}
