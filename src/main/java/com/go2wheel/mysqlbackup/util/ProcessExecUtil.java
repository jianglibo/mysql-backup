package com.go2wheel.mysqlbackup.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.go2wheel.mysqlbackup.aop.MeasureTimeCost;
import com.go2wheel.mysqlbackup.value.ProcessExecResult;

@Service
public class ProcessExecUtil {

	public ProcessExecResult runDos(String... cmds) {

		if (cmds.length < 1) {
			return new ProcessExecResult(-2);
		}

		if (!("cmd".equals(cmds[0]) || "cmd.exe".equals(cmds[0]))) {
			String[] nn = new String[2 + cmds.length];
			nn[0] = "cmd.exe";
			nn[1] = "/C";
			System.arraycopy(cmds, 0, nn, 2, cmds.length);
			cmds = nn;
		}
		return run(Arrays.asList(cmds));
	}

	@MeasureTimeCost
	public ProcessExecResult run(List<String> cmds) {
		return run(cmds, null);
	}
	
	@MeasureTimeCost
	public ProcessExecResult run(List<String> cmds, Path stdOutFile) {
		ProcessBuilder pb = new ProcessBuilder(cmds);
		ProcessExecResult per = new ProcessExecResult();

		// Getting the results
		try {
			if (stdOutFile != null) {
				pb.redirectOutput(stdOutFile.toFile());
			}
			Process process = pb.start();
			List<String> stdOutLines = new ArrayList<>();
			String line;
			BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
			while ((line = stdout.readLine()) != null) {
				stdOutLines.add(line);
			}
			stdout.close();

			List<String> stdErrorLines = new ArrayList<>();
			BufferedReader stderr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			while ((line = stderr.readLine()) != null) {
				stdErrorLines.add(line);
			}
			stderr.close();
			process.waitFor();
			per.setStdOut(stdOutLines);
			per.setStdError(stdErrorLines);
			per.setExitValue(process.exitValue());
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			per.setException(e);
		}
		return per;
	}
	
	@MeasureTimeCost
	public ProcessExecResult roboCopy(String src, String dst, Path stdOutFile) {
		List<String> cmds = Arrays.asList("Robocopy.exe", src, dst, "/E", "/COPY:DAT", "/PURGE");
		return run(cmds, stdOutFile);
	}

}
