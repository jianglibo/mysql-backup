package com.go2wheel.mysqlbackup.powershell;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Map;

import org.junit.Test;
import org.quartz.SchedulerException;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.exception.RunRemoteCommandException;
import com.go2wheel.mysqlbackup.util.PSUtil;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.jcraft.jsch.JSchException;

public class TestSchedulerJob extends SpringBaseFort {
	
	private static final String JOB_NAME = "T_J_N";
	

	
//	Register-ScheduledJob
//    [-ScriptBlock] <ScriptBlock>
//    [-Name] <String>
//    [-Trigger <ScheduledJobTrigger[]>]
//    [-InitializationScript <ScriptBlock>]
//    [-RunAs32]
//    [-Credential <PSCredential>]
//    [-Authentication <AuthenticationMechanism>]
//    [-ScheduledJobOption <ScheduledJobOptions>]
//    [-ArgumentList <Object[]>]
//    [-MaxResultCount <Int32>]
//    [-RunNow]
//    [-RunEvery <TimeSpan>]
//    [-WhatIf]
//    [-Confirm]
//    [<CommonParameters>]
    		
	@Test
	public void t() throws JSchException, SchedulerException, RunRemoteCommandException, IOException {
//		Register-ScheduledJob -ScriptBlock {Get-Process} -Name T_J_N -RunNow -RunEvery (New-TimeSpan -Minutes 1)
//		Unregister-ScheduledJob -Name T_J_N
//		PSUtil.runPsCommand("New-JobTrigger -Once -At (Get-Date) -RepetitionInterval (New-TimeSpan -Hour 12) -RepetitionDuration ([TimeSpan]::MaxValue)");
		createSessionLocalHostWindowsAfterClear();
		String command = String.format("Unregister-ScheduledJob -Name %s", JOB_NAME);
		SSHcommonUtil.runRemoteCommand(session, command);
		command = String.format("Register-ScheduledJob -ScriptBlock {Get-Process} -Name %s -RunNow -RunEvery (New-TimeSpan -Minutes 1)", JOB_NAME);
		RemoteCommandResult rcr = SSHcommonUtil.runRemoteCommand(session, command);
		assertThat(rcr.getExitValue(), equalTo(0));
		command = String.format("Get-Job -Name %s | format-list", JOB_NAME);
		rcr = SSHcommonUtil.runRemoteCommand(session, command);
		Map<String, String> map = PSUtil.parseFormatList(rcr.getAllTrimedNotEmptyLines()).get(0);
		assertThat(map.get("Name"), equalTo(JOB_NAME));
		
		command = "Get-ScheduledJob |Out-Null;Receive-Job -Name T_J_N | Measure-Object";
		rcr = SSHcommonUtil.runRemoteCommand(session, command);
		map = PSUtil.parseFormatList(rcr.getAllTrimedNotEmptyLines()).get(0);
		
		assertThat(Integer.valueOf(map.get("Count")), greaterThan(100));

	}

}
