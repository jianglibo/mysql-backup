package com.go2wheel.mysqlbackup.util;

import java.util.List;

import com.go2wheel.mysqlbackup.executablerunner.ExecutableRunnerSsh;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;

public class ExecutorUtil {
	
	public static <T> RemoteCommandResult<T> runListOfCommands(List<ExecutableRunnerSsh<T>> commands) {
		RemoteCommandResult<T> er = null;
		for(ExecutableRunnerSsh<T> command: commands) {
			command.setPrevResult(er);
			er = command.execute();
			if (!er.isSuccess()) {
				return er;
			}
		}
		return er;
	}
	

}
