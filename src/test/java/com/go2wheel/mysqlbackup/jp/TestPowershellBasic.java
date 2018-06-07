package com.go2wheel.mysqlbackup.jp;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.junit.Test;

import com.go2wheel.mysqlbackup.util.StringUtil;
import com.profesorfalken.jpowershell.PowerShell;
import com.profesorfalken.jpowershell.PowerShellResponse;

public class TestPowershellBasic {

	@Test
	public void tGetProcess() {
		PowerShellResponse response = PowerShell.executeSingleCommand("Get-Process");
		String so = response.getCommandOutput();
		assertThat(so.length(), greaterThan(10));
	}

	@Test
	public void tpsDriver() {
		String cmd = "Get-PSDrive | Where-Object name -Match \"^.{1}$\" | Select-Object name, used, free";
		PowerShellResponse response = PowerShell.executeSingleCommand(cmd);
		String so = response.getCommandOutput();
		List<String> ss = StringUtil.splitLines(so);
		String line1 = ss.get(0);
		String line2 = ss.get(1);
		assertTrue(line1.contains("Name") && line1.contains("Used") && line1.contains("Free"));
	}

	@Test
	public void testProcessBuilder() throws IOException {
		ProcessBuilder pb = new ProcessBuilder("powershell.exe",
				"Get-PSDrive | Where-Object name -Match '^.{1}$' | Select-Object name, used, free");
		Process powerShellProcess = pb.start();

		// Getting the results
		powerShellProcess.getOutputStream().close();
		String line;
		System.out.println("Standard Output:");
		BufferedReader stdout = new BufferedReader(new InputStreamReader(powerShellProcess.getInputStream()));
		while ((line = stdout.readLine()) != null) {
			System.out.println(line);
		}
		stdout.close();
		System.out.println("Standard Error:");
		BufferedReader stderr = new BufferedReader(new InputStreamReader(powerShellProcess.getErrorStream()));
		while ((line = stderr.readLine()) != null) {
			System.out.println(line);
		}
		stderr.close();
		System.out.println("Done");
	}

	@Test
	public void testPsDriverRawJava() throws IOException {
		// String command = "powershell.exe your command";
		// Getting the version
		// String command = "powershell.exe $PSVersionTable.PSVersion";
		String command = "powershell.exe \"Get-PSDrive | Where-Object name -Match '^.{1}$' | Select-Object name, used, free\"";
		// Executing the command

		Process powerShellProcess = Runtime.getRuntime().exec(command);
		// Getting the results
		powerShellProcess.getOutputStream().close();
		String line;
		System.out.println("Standard Output:");
		BufferedReader stdout = new BufferedReader(new InputStreamReader(powerShellProcess.getInputStream()));
		while ((line = stdout.readLine()) != null) {
			System.out.println(line);
		}
		stdout.close();
		System.out.println("Standard Error:");
		BufferedReader stderr = new BufferedReader(new InputStreamReader(powerShellProcess.getErrorStream()));
		while ((line = stderr.readLine()) != null) {
			System.out.println(line);
		}
		stderr.close();
		System.out.println("Done");

	}

}