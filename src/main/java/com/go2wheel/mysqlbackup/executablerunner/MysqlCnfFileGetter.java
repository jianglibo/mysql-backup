package com.go2wheel.mysqlbackup.executablerunner;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MysqlCnfFileGetter implements ExecutableRunner<List<String>> {
	
	private static String matcherline = "Default options are read from the following"; 

	@Override
	public ExecuteResult<List<String>> execute(String... commandWords) {
		if (commandWords.length == 0) {
			commandWords = new String[] {"mysql", "--help", "--verbose"};
		}
		ProcessBuilder pb = new ProcessBuilder(commandWords);
		try {
			Process process = pb.start();
			List<String> resultLines = getWhatProcessWriteOut(process.getInputStream());
			List<String> errorLines = getWhatProcessWriteOut(process.getErrorStream());
			PrintWriter pw = getProcessWriter(process.getOutputStream());
			pw.write("123456");
		    int exitValue = process.waitFor();
		    process.isAlive();
		    List<String> cfgfiles = new ArrayList<>();
		    for (int i = 0; i < resultLines.size(); i++) {
				if (resultLines.get(i).indexOf(matcherline) != -1) {
					if (i + 1 < resultLines.size()) {
						String line = resultLines.get(i + 1);
						String[] files = line.split("\\s+");
						cfgfiles = Arrays.asList(files);
						break;
					}
				}
			}
		    
		    return new ExecuteResult<>(cfgfiles, exitValue);
			
		} catch (IOException | InterruptedException e) {
			return ExecuteResult.failedResult(e.getMessage());
		}
	}

}
