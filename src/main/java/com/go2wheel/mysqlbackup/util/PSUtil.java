package com.go2wheel.mysqlbackup.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.go2wheel.mysqlbackup.value.ProcessExecResult;

public class PSUtil {
	
	private static String POWERSHELL_EXE = "powershell.exe";
	
	public static ProcessExecResult invokePowershell(List<String> commandElements, Charset cs) {
		ProcessExecResult per = new ProcessExecResult();
		try {
			ProcessBuilder pb = new ProcessBuilder(commandElements.toArray(new String[] {}));
			Process powerShellProcess = pb.start();
			powerShellProcess.getOutputStream().close();
			String line;
			List<String> stdOutLines = new ArrayList<>();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(powerShellProcess.getInputStream(), cs));
			while ((line = stdout.readLine()) != null) {
				stdOutLines.add(line);
			}
			stdout.close();
			List<String> stdErrorLines = new ArrayList<>();
			BufferedReader stderr = new BufferedReader(new InputStreamReader(powerShellProcess.getErrorStream(), cs));
			while ((line = stderr.readLine()) != null) {
				stdErrorLines.add(line);
			}
			stderr.close();
			powerShellProcess.waitFor();
			per.setStdOut(stdOutLines);
			per.setStdError(stdErrorLines);
			per.setExitValue(powerShellProcess.exitValue());
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			per.setException(e);
		}
		return per;
	}
	
	public static ProcessExecResult invokePowershell(String powershell, List<String> commandElements, Charset cs) {
		commandElements.add(0, powershell);
		return invokePowershell(commandElements, cs);
	}

	
	public static ProcessExecResult runPsCommand(String oneLineCommand) {
		List<String> ls = new ArrayList<>();
		ls.add(POWERSHELL_EXE);
		ls.add(oneLineCommand);
		return invokePowershell(ls, Charset.defaultCharset());
	}
	
	public static ProcessExecResult runPsFile(String filepath, Charset cs, String...others) {
		return runPsFile("powershell.exe", filepath, cs, others);
	}
	public static ProcessExecResult runPsFile(String powershell, String filepath, Charset cs, String...others) {
		List<String> ls = new ArrayList<>();
		ls.add(powershell);
		ls.add("-File");
		ls.add(filepath);
		ls.addAll(Arrays.asList(others));
		return invokePowershell(ls, cs);
	}
	
	public static ProcessExecResult runPsCommandByCall(String oneLineCommand) {
		return runPsCommandByCall(oneLineCommand, Charset.defaultCharset());
	}
	
	public static ProcessExecResult runPsCommandByCall(String oneLineCommand, Charset cs) {
		return runPsCommandByCall("powershell.exe", oneLineCommand, Charset.defaultCharset());
	}

	
	public static ProcessExecResult runPsCommandByCall(String powershell, String oneLineCommand, Charset cs) {
		List<String> ls = new ArrayList<>();
		ls.add(powershell);
		ls.add("-Command");
		ls.add("& {" + oneLineCommand + "}");
		return invokePowershell(ls, cs);
	}
	
	public static ProcessExecResult archiveZip(String src, String dst) {
		String cmd = String.format("Compress-Archive -Path %s -DestinationPath %s", src, dst);
		return runPsCommand(cmd);
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
