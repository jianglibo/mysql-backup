package com.go2wheel.mysqlbackup.jp;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.go2wheel.mysqlbackup.util.PSUtil;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.profesorfalken.jpowershell.PowerShell;
import com.profesorfalken.jpowershell.PowerShellNotAvailableException;
import com.profesorfalken.jpowershell.PowerShellResponse;

public class TestPowershellBasic {

	@Test
	public void tGetProcess() {
		PowerShellResponse response = PowerShell.executeSingleCommand("Get-Process");
		String so = response.getCommandOutput();
		assertThat(so.length(), greaterThan(10));
	}

	@Test(expected = ArrayIndexOutOfBoundsException.class)
	public void tpsDriver() {

		
	   PowerShell powerShell = null;
	   try {
	       powerShell = PowerShell.openSession();
	       String cmd = "Get-PSDrive | Where-Object name -Match '^.{1}$' | Select-Object name, used, free";
	       
	       PowerShellResponse response = powerShell.executeCommand(cmd);
	       powerShell.executeScript(new BufferedReader(new StringReader(cmd)));
	       
			String so = response.getCommandOutput();
			List<String> ss = StringUtil.splitLines(so);
			String line1 = ss.get(0);
			String line2 = ss.get(1);
			assertTrue(line1.contains("Name") && line1.contains("Used") && line1.contains("Free"));
	       
	       System.out.println("List Processes:" + response.getCommandOutput());
	       
	       response = powerShell.executeCommand("Get-WmiObject Win32_BIOS");
	       
	       System.out.println("BIOS information:" + response.getCommandOutput());
	   } catch(PowerShellNotAvailableException ex) {
	   } finally {
	       if (powerShell != null)
	         powerShell.close();
	   }
	}

	@Test()
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
	public void testProcessBuilderNotClose() throws IOException {
		ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-Command", "{Get-PSDrive | Where-Object name -Match '^.{1}$' | Select-Object name, used, free}") ;
		String cmd = "";
		Process powerShellProcess = pb.start();

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
	
//	@Test
//	public void testProcessBuilderInteractive() throws IOException {
//		ProcessBuilder pb = new ProcessBuilder("powershell.exe","-NoExit", "-InputFormat", "Text",  "-Command", "-") ;
//		String cmd = "Get-PSDrive | Where-Object name -Match '^.{1}$' | Select-Object name, used, free";
//		Process powerShellProcess = pb.start();
//
//		// Getting the results
//		PrintWriter pw = new PrintWriter(powerShellProcess.getOutputStream());
////		.close();
//		String line;
//		pw.println(cmd);
//		System.out.println("Standard Output:");
//
//		BufferedReader stdout = new BufferedReader(new InputStreamReader(powerShellProcess.getInputStream()));
////		pw.println();
//		while ((line = stdout.readLine()) != null) {
//			System.out.println(line);
//		}
//		stdout.close();
//		System.out.println("Standard Error:");
//		BufferedReader stderr = new BufferedReader(new InputStreamReader(powerShellProcess.getErrorStream()));
//		while ((line = stderr.readLine()) != null) {
//			System.out.println(line);
//		}
//		stderr.close();
////		pw.println(cmd);
//		System.out.println("Done");
//	}

	@Test
	public void testPsDriverRawJava() throws IOException {
		String command = "Get-PSDrive | Where-Object name -Match '^.{1}$' | Select-Object name, used, free";
		RemoteCommandResult rcr = PSUtil.runPsCommand(command);
		List<String> ls = rcr.getAllTrimedNotEmptyLines();
		assertTrue(ls.get(0).contains("Name"));

	}
	
	@Test
	public void testFormatList() throws IOException {
		String pscommand = "Get-PSDrive | Where-Object Name -Match '^.{1}$' | Format-List -Property *";
		RemoteCommandResult rcr = PSUtil.runPsCommand(pscommand);
		
		List<Map<String, String>> lmss = PSUtil.parseFormatList(rcr.getStdOutList());
		assertThat(lmss.size(), greaterThan(1));
		
	}

}
