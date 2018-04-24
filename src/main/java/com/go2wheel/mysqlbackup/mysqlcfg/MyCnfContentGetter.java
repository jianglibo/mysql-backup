package com.go2wheel.mysqlbackup.mysqlcfg;

import java.util.List;

import com.go2wheel.mysqlbackup.executablerunner.ExecutableRunnerSshBase;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.jcraft.jsch.Session;
import com.go2wheel.mysqlbackup.value.Box;

public class MyCnfContentGetter extends ExecutableRunnerSshBase {

	public MyCnfContentGetter(Session sshSession, Box box, RemoteCommandResult<List<String>> prevResult) {
		super(sshSession, box, prevResult);
	}


	public MyCnfContentGetter(Session sshSession, Box box) {
		super(sshSession, box);
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
