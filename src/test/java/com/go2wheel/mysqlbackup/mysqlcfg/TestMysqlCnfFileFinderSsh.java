package com.go2wheel.mysqlbackup.mysqlcfg;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.go2wheel.mysqlbackup.executablerunner.ExecutableRunnerSshBase;
import com.go2wheel.mysqlbackup.sshj.SshBaseFort;
import com.go2wheel.mysqlbackup.value.ExternalExecuteResult;

public class TestMysqlCnfFileFinderSsh extends SshBaseFort {
	
	@Test
	public void t() throws IOException {
		MysqlCnfFileLister mcfg = new MysqlCnfFileLister(sshClient, demoInstance);
		ExternalExecuteResult<List<String>> er = mcfg.execute();
		assertFalse("reason shouldn't present.", er.getReason().isPresent());
		assertTrue("invoke should be successed.", er.isSuccess());
		
		assertThat(er.getResult().size(), greaterThan(0));
		
		String lscmd = "ls " + String.join(" ", er.getResult());
		
		er = new ExecutableRunnerSshBase(sshClient, demoInstance) {
			
			@Override
			protected String[] getLinesToFeed() {
				return null;
			}
			
			@Override
			protected String getCommandString() {
				return lscmd;
			}
			
			@Override
			protected ExternalExecuteResult<List<String>> afterSuccessInvoke(
					ExternalExecuteResult<List<String>> externalExecuteResult) {
				String f = externalExecuteResult.getResult().stream().filter(line -> line.indexOf("No such file or directory") == -1).findFirst().get();
				externalExecuteResult.setResult(Arrays.asList(f));
				return externalExecuteResult;
			}
		}.execute();
		
		assertThat(er.getResult().size(), equalTo(1));
		assertThat(er.getResult().get(0), equalTo("/etc/my.cnf"));
		
	}

}
