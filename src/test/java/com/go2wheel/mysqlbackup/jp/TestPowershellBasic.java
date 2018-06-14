package com.go2wheel.mysqlbackup.jp;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.go2wheel.mysqlbackup.util.PSUtil;
import com.go2wheel.mysqlbackup.value.ProcessExecResult;
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
	public void testPsDriverRawJava() throws IOException {
		String command = "Get-PSDrive | Where-Object name -Match '^.{1}$' | Select-Object name, used, free";
		ProcessExecResult rcr = PSUtil.runPsCommand(command);
		List<String> ls = rcr.getStdOutFilterEmpty();
		assertTrue(ls.get(0).contains("Name"));

	}
	
	@Test
	public void testFormatList() throws IOException {
		String pscommand = "Get-PSDrive | Where-Object Name -Match '^.{1}$' | Format-List -Property *";
		ProcessExecResult rcr = PSUtil.runPsCommand(pscommand);
		
		List<Map<String, String>> lmss = PSUtil.parseFormatList(rcr.getStdOut());
		assertThat(lmss.size(), greaterThan(1));
		
	}

}
