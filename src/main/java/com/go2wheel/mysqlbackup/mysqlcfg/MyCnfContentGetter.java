package com.go2wheel.mysqlbackup.mysqlcfg;

import java.util.List;

import com.go2wheel.mysqlbackup.executablerunner.ExecutableRunnerSshBase;
import com.go2wheel.mysqlbackup.value.ExternalExecuteResult;

import net.schmizz.sshj.SSHClient;

public class MyCnfContentGetter extends ExecutableRunnerSshBase {

	public MyCnfContentGetter(SSHClient sshClient) {
		super(sshClient);
	}


	@Override
	protected String getCommandString() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	protected ExternalExecuteResult<List<String>> afterSuccessInvoke(ExternalExecuteResult<List<String>> executeInternal) {
		return null;
	}


	@Override
	protected String[] getLinesToFeed() {
		// TODO Auto-generated method stub
		return null;
	}
	


}
