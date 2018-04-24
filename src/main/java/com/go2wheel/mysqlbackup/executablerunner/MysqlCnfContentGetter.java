package com.go2wheel.mysqlbackup.executablerunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.go2wheel.mysqlbackup.value.RemoteCommandResult;

public class MysqlCnfContentGetter implements ExecutableRunner<List<String>> {
	
	@Override
	public RemoteCommandResult<List<String>> execute(String...cnfFile) {
		if (cnfFile.length != 1) {
			return RemoteCommandResult.failedResult(String.format("Can read Only one file. But %s", cnfFile.length));
		}
		try {
			Path p = Paths.get(cnfFile[0]);
			List<String> lines = Files.readAllLines(p);
		    return new RemoteCommandResult<>(lines, 0);
			
		} catch (IOException e) {
			return RemoteCommandResult.failedResult(e.getMessage());
		}
	}
	
	public void writeToInstancesFolder() {
		
	}

}
