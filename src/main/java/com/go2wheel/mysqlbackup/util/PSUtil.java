package com.go2wheel.mysqlbackup.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		return new RemoteCommandResult(String.join("\n", lines), String.join("\n", errorLines),
				powerShellProcess.exitValue());
	}

	public static List<Map<String, String>> parseFormatList(List<String> lines) {
		List<Map<String, String>> lmss = new ArrayList<>();
		Map<String, String> one = new HashMap<>();

		String firstKye = null, key, value;

		for (String line : lines) {
			String[] ss = line.split(":", 2);
			if (ss.length == 2) {
				key = ss[0].trim();
				value = ss[1].trim();
				if (firstKye == null) {
					firstKye = key;
				} else if (firstKye.equals(key)) {
					lmss.add(one);
					one = new HashMap<>();
				}
				one.put(key, value);
			}
		}
		if (!one.isEmpty()) {
			lmss.add(one);
		}
		return lmss;
	}

}
