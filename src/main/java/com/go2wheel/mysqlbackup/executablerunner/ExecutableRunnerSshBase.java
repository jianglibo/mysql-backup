package com.go2wheel.mysqlbackup.executablerunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.go2wheel.mysqlbackup.util.SSHcommonUtil;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.value.RemoteCommandResult;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.go2wheel.mysqlbackup.value.Box;



public abstract class ExecutableRunnerSshBase implements ExecutableRunnerSsh {
	
	protected Session sshSession;
	
	protected RemoteCommandResult prevResult;
	
	protected Box box;
	
	public ExecutableRunnerSshBase(Session sshSession,Box box) {
		this.sshSession = sshSession;
		this.box = box;
		this.prevResult = null;
	}
	
	public ExecutableRunnerSshBase(Session sshClient,Box box, RemoteCommandResult prevResult) {
		this.sshSession = sshClient;
		this.prevResult = prevResult;
		this.box = box;
	}
	
	@Override
	public void setPrevResult(RemoteCommandResult prevResult) {
		this.prevResult = prevResult;
	}

	@Override
	public RemoteCommandResult execute() {
		String reason = "";
		try {
				RemoteCommandResult er = SSHcommonUtil.runRemoteCommand(sshSession, getCommandString()); 
					return afterSuccessInvoke(er);

		} catch (Exception e) {
		}
		return RemoteCommandResult.failedResult(reason);
	}
	
	protected abstract RemoteCommandResult afterSuccessInvoke(RemoteCommandResult externalExecuteResult);
	
//	protected RemoteCommandResult executeInternal(Channel channel, Box box) throws IOException, JSchException {
//		if (getCommandString() == null || getCommandString().trim().isEmpty()) {
//			return RemoteCommandResult.failedResult("empty ssh command invoked.");
//		}
//		((ChannelExec) channel).setCommand(getCommandString());
//		channel.setInputStream(null);
//		((ChannelExec) channel).setErrStream(System.err);
//		InputStream in = channel.getInputStream();
//		channel.connect();
//		RemoteCommandResult cmdOut = SSHcommonUtil.readChannelOutput(channel, in);
//
//		return new RemoteCommandResult(StringUtil.splitLines(cmdOut.getResult()), cmdOut.getExitValue());
//	}
	
	protected abstract String[] getLinesToFeed();
	protected abstract String getCommandString();

}
