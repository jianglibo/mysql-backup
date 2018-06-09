package com.go2wheel.mysqlbackup.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.go2wheel.mysqlbackup.value.RemoteCommandResult;

public class PSUtil {
	
	public static RemoteCommandResult runPsCommand(String command) throws IOException {
		
		ProcessBuilder pb = new ProcessBuilder("powershell.exe", command);
		Process powerShellProcess = pb.start();
		
		// Getting the results
		powerShellProcess.getOutputStream().close();
		List<String> lines = new ArrayList<>();
		String line;
		BufferedReader stdout = new BufferedReader(new InputStreamReader(powerShellProcess.getInputStream()));
		while ((line = stdout.readLine()) != null) {
			lines.add(line);
		}
		stdout.close();
		
		List<String> errorLines = new ArrayList<>();
		
		BufferedReader stderr = new BufferedReader(new InputStreamReader(powerShellProcess.getErrorStream()));
		while ((line = stderr.readLine()) != null) {
			errorLines.add(line);
		}
		stderr.close();
		return new RemoteCommandResult(String.join("\n", lines), String.join("\n", errorLines), powerShellProcess.exitValue());
	}

}
