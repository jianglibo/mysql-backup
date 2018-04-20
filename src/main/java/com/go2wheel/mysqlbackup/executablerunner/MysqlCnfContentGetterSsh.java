package com.go2wheel.mysqlbackup.executablerunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.go2wheel.mysqlbackup.MyAppSettings;
import com.go2wheel.mysqlbackup.value.ExternalExecuteResult;
import com.go2wheel.mysqlbackup.value.MysqlInstance;

public class MysqlCnfContentGetterSsh implements ExecutableRunnerSsh<List<String>> {
	
	private MyAppSettings myAppSettings;
	
//	@Override
//	public ExternalExecuteResult<List<String>> execute(String...cnfFile) {
//		if (cnfFile.length != 1) {
//			return ExternalExecuteResult.failedResult(String.format("Can read Only one file. But %s", cnfFile.length));
//		}
//		try {
//			Path p = Paths.get(cnfFile[0]);
//			List<String> lines = Files.readAllLines(p);
//		    return new ExternalExecuteResult<>(lines, 0);
//			
//		} catch (IOException e) {
//			return ExternalExecuteResult.failedResult(e.getMessage());
//		}
//	}
	
	public void writeToInstancesFolder() {
		
	}

	@Autowired
	public void setMyAppSettings(MyAppSettings myAppSettings) {
		this.myAppSettings = myAppSettings;
	}

	@Override
	public ExternalExecuteResult<List<String>> execute(MysqlInstance instance, String commandString) {
		return null;
	}

}
