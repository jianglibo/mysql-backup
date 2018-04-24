package com.go2wheel.mysqlbackup.mysqlcfg;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.go2wheel.mysqlbackup.executablerunner.ExecutableRunnerSshBase;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.go2wheel.mysqlbackup.value.Box;

import net.schmizz.sshj.SSHClient;

public class MysqlCnfFileLister extends ExecutableRunnerSshBase {
	
	private Logger logger = LoggerFactory.getLogger(MysqlCnfFileLister.class);
	
	public MysqlCnfFileLister(SSHClient sshClient, Box box, RemoteCommandResult<List<String>> prevResult) {
		super(sshClient, box, prevResult);
	}

	public MysqlCnfFileLister(SSHClient sshClient, Box box) {
		super(sshClient, box);
	}

	private static String matcherline = "Default options are read from the following";

	@Override
	protected String getCommandString() {
		return "mysql --help --verbose";
	}

	@Override
	protected RemoteCommandResult<List<String>> afterSuccessInvoke(RemoteCommandResult<List<String>> externalExecuteResult) {
		List<String> lines = externalExecuteResult.getResult();
		String targetLine = null;
		int maxIndex = lines.size();
		for(int i =0; i< maxIndex; i++) {
			String line = lines.get(i);
			if (line.indexOf(matcherline) != -1) {
				if (i + 1 < maxIndex) {
					targetLine = lines.get(i + 1);
					break;
				}
			}
		}
		if (targetLine != null) {
			List<String> filenames = Stream.of(targetLine.split("\\s+")).filter(l -> !l.trim().isEmpty()).collect(Collectors.toList());
			externalExecuteResult.setResult(filenames);
		} else {
			logger.error("Cannot find '{}' in returned lines from invokeing {}.", matcherline, "mysql --help --verbose");
		}
		return externalExecuteResult;
	}

	@Override
	protected String[] getLinesToFeed() {
		return null;
	}
}
