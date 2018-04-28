package com.go2wheel.mysqlbackup.expect;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import org.junit.Test;

import com.go2wheel.mysqlbackup.jsch.SshBaseFort;
import com.go2wheel.mysqlbackup.util.Md5Checksum;
import com.go2wheel.mysqlbackup.util.MysqlUtil;
import com.go2wheel.mysqlbackup.util.ScpUtil;
import com.go2wheel.mysqlbackup.util.StringUtil.LinuxFileInfo;
import com.jcraft.jsch.JSchException;

public class TestMysqlDumpExpect extends SshBaseFort {
	
	@Test
	public void t() throws Exception {
		createALocalFile(" ");
		MysqlDumpExpect mde = new MysqlDumpExpect(session, box);
		Optional<LinuxFileInfo> result = mde.start();
		assertTrue(result.isPresent());
		ScpUtil.from(session, MysqlUtil.DUMP_FILE_NAME, tmpFile.toAbsolutePath().toString()); 
		
		assertThat(Files.size(tmpFile), equalTo(result.get().getSize()));
		
		String md5 = Md5Checksum.getMD5Checksum(tmpFile.toString());
		
		assertThat(md5, equalTo(result.get().getMd5()));
	
	}

}
