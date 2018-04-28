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
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import com.go2wheel.mysqlbackup.jsch.SshBaseFort;
import com.go2wheel.mysqlbackup.util.Md5Checksum;
import com.go2wheel.mysqlbackup.util.MysqlUtil;
import com.go2wheel.mysqlbackup.util.ScpUtil;
import com.go2wheel.mysqlbackup.util.StringUtil.LinuxFileInfo;
import com.jcraft.jsch.JSchException;

public class TestMysqlFlushLogExpect extends SshBaseFort {
	
	@Test
	public void t() throws Exception {
		
		MysqlUtil mysqlUtil = new MysqlUtil();
		mysqlUtil.setAppSettings(appSettings);
		
		createALocalFile(" ");
		MysqlFlushLogExpect mfe = new MysqlFlushLogExpect(session, box);
		List<String> lines = mfe.start();
		
		MysqlFlushLogExpect mfe1 = new MysqlFlushLogExpect(session, box);
		List<String> lines1 = mfe.start();
		
		assertThat(lines.size(), equalTo(lines1.size() - 1));
		
		mysqlUtil.writeBinLogIndex(session, box, lines1);
	}

}
