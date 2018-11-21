package com.go2wheel.mysqlbackup.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.go2wheel.mysqlbackup.value.ProcessExecResult;

public class PSUtil {
	
	public static ProcessExecResult runPsCommand(String oneLineCommand) {
		ProcessExecResult per = new ProcessExecResult();
		try {
			ProcessBuilder pb = new ProcessBuilder("powershell.exe", oneLineCommand);
			Process powerShellProcess = pb.start();
			powerShellProcess.getOutputStream().close();
			String line;
			List<String> stdOutLines = new ArrayList<>();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(powerShellProcess.getInputStream()));
			while ((line = stdout.readLine()) != null) {
				stdOutLines.add(line);
			}
			stdout.close();
			List<String> stdErrorLines = new ArrayList<>();
			BufferedReader stderr = new BufferedReader(new InputStreamReader(powerShellProcess.getErrorStream()));
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
	
	public static ProcessExecResult runPsFile(String filepath, Charset cs, String...others) {
		return runPsFile("powershell.exe", filepath, cs, others);
	}
	public static ProcessExecResult runPsFile(String powershell, String filepath, Charset cs, String...others) {
		ProcessExecResult per = new ProcessExecResult();
		try {
			String[] cmds=new String[others.length + 3];
					cmds[0] = powershell;
					cmds[1] = "-File";
					
					cmds[2] = filepath;
			for(int i = 0; i < others.length; i++) {
				cmds[i+3] = others[i];
			}
			ProcessBuilder pb = new ProcessBuilder(cmds);
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
	
	public static ProcessExecResult runPsCommandByCall(String oneLineCommand) {
		return runPsCommandByCall(oneLineCommand, Charset.defaultCharset());
	}
	
	public static ProcessExecResult runPsCommandByCall(String oneLineCommand, Charset cs) {
		return runPsCommandByCall("powershell.exe", oneLineCommand, Charset.defaultCharset());
	}

	
	public static ProcessExecResult runPsCommandByCall(String powershell, String oneLineCommand, Charset cs) {
		ProcessExecResult per = new ProcessExecResult();
		try {
			ProcessBuilder pb = new ProcessBuilder(powershell,"-Command", "& {" + oneLineCommand + "}");
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
