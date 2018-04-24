package com.go2wheel.mysqlbackup.mysqlcfg;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.go2wheel.mysqlbackup.executablerunner.ExecutableRunnerSshBase;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.go2wheel.mysqlbackup.value.Box;

import net.schmizz.sshj.SSHClient;

public class MyCnfFirstExist extends ExecutableRunnerSshBase {

	public MyCnfFirstExist(SSHClient sshClient, Box box, RemoteCommandResult<List<String>> prevResult) {
		super(sshClient, box, prevResult);
	}

	public MyCnfFirstExist(SSHClient sshClient, Box box) {
		super(sshClient, box);
	}


	@Override
	protected String getCommandString() {
		return "ls " + String.join(" ", prevResult.getResult());
	}


	@Override
	protected RemoteCommandResult<List<String>> afterSuccessInvoke(RemoteCommandResult<List<String>> externalExecuteResult) {
		Optional<String> firstExists = externalExecuteResult.getResult().stream().filter(line -> line.indexOf("No such file or directory") == -1).findFirst();
		if (firstExists.isPresent()) {
			externalExecuteResult.setResult(Arrays.asList(firstExists.get()));
			return externalExecuteResult;
		} else {
			return RemoteCommandResult.failedResult("found mysql config file failed.");
		}
	}


	@Override
	protected String[] getLinesToFeed() {
		return null;
	}
	


}
