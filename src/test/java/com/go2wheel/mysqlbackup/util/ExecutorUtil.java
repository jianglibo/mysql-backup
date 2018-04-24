package com.go2wheel.mysqlbackup.util;

import java.util.List;

import com.go2wheel.mysqlbackup.executablerunner.ExecutableRunnerSsh;
import com.go2wheel.mysqlbackup.value.ExternalExecuteResult;

public class ExecutorUtil {
	
	public static <T> ExternalExecuteResult<T> runListOfCommands(List<ExecutableRunnerSsh<T>> commands) {
		ExternalExecuteResult<T> er = null;
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
