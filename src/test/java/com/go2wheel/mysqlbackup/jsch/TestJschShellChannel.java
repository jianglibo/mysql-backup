package com.go2wheel.mysqlbackup.jsch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.junit.Test;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;

public class TestJschShellChannel extends SpringBaseFort {

	@Test
	public void t() throws JSchException, IOException {
	      Channel channel = session.openChannel("shell");
	      OutputStream ops = channel.getOutputStream();
	      PrintStream ps = new PrintStream(ops, true);
	      
	      ((ChannelShell) channel).setPtyType("dumb");

	      channel.connect();
	      InputStream input = channel.getInputStream();
//	      InputStream inputext = channel.getExtInputStream();
	      //commands
	      ps.println("cd /etc");
	      RemoteCommandResult string = SSHcommonUtil.readChannelOutputDoBest(channel, input, ".*#\\s*$");
	      System.out.println(string.getStdOut());
	      ps.println("ls");
	      string = SSHcommonUtil.readChannelOutputDoBest(channel, input, ".*#\\s*$");
	      System.out.println(string.getStdOut());
	      ps.close();

	      channel.disconnect();
	}
}
