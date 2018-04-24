package com.go2wheel.mysqlbackup.mysqlcfg;

import java.util.List;

import com.go2wheel.mysqlbackup.executablerunner.ExecutableRunnerSshBase;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.go2wheel.mysqlbackup.value.Box;

import net.schmizz.sshj.SSHClient;

public class MyCnfContentGetter extends ExecutableRunnerSshBase {

	public MyCnfContentGetter(SSHClient sshClient, Box box, RemoteCommandResult<List<String>> prevResult) {
		super(sshClient, box, prevResult);
	}


	public MyCnfContentGetter(SSHClient sshClient, Box box) {
		super(sshClient, box);
	}


	@Override
	protected String getCommandString() {
		return String.format("cat %s", prevResult.getResult().get(0));
	}


	@Override
	protected RemoteCommandResult<List<String>> afterSuccessInvoke(RemoteCommandResult<List<String>> externalExecuteResult) {
		return externalExecuteResult;
	}


	@Override
	protected String[] getLinesToFeed() {
		return null;
	}

}
