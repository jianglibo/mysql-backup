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



public abstract class ExecutableRunnerSshBase implements ExecutableRunnerSsh<List<String>> {
	
	protected Session sshSession;
	
	protected RemoteCommandResult<List<String>> prevResult;
	
	protected Box box;
	
	public ExecutableRunnerSshBase(Session sshSession,Box box) {
		this.sshSession = sshSession;
		this.box = box;
		this.prevResult = null;
	}
	
	public ExecutableRunnerSshBase(Session sshClient,Box box, RemoteCommandResult<List<String>> prevResult) {
		this.sshSession = sshClient;
		this.prevResult = prevResult;
		this.box = box;
	}
	
	@Override
	public void setPrevResult(RemoteCommandResult<List<String>> prevResult) {
		this.prevResult = prevResult;
	}

	@Override
	public RemoteCommandResult<List<String>> execute() {
		String reason = "";
		try {
			final Channel channel = sshSession.openChannel("exec");
			try {
				RemoteCommandResult<List<String>> er = executeInternal(channel, box);
				if (er.isSuccess()) {
					RemoteCommandResult<List<String>> er1 = afterSuccessInvoke(er);
					return er1 == null ? er : er1;
				} else {
					return er;
				}
				
			} catch (IOException e) {
				reason = e.getMessage();
				e.printStackTrace();
			} finally {
				channel.disconnect();
			}
		} catch (Exception e) {
		}
		return RemoteCommandResult.failedResult(reason);
	}
	
	protected abstract RemoteCommandResult<List<String>> afterSuccessInvoke(RemoteCommandResult<List<String>> externalExecuteResult);
	
	protected RemoteCommandResult<List<String>> executeInternal(Channel channel, Box box) throws IOException, JSchException {
		if (getCommandString() == null || getCommandString().trim().isEmpty()) {
			return RemoteCommandResult.failedResult("empty ssh command invoked.");
		}
		((ChannelExec) channel).setCommand(getCommandString());
		channel.setInputStream(null);
		((ChannelExec) channel).setErrStream(System.err);
		InputStream in = channel.getInputStream();
		channel.connect();
		RemoteCommandResult<String> cmdOut = SSHcommonUtil.readChannelOutput(channel, in);

		return new RemoteCommandResult<List<String>>(StringUtil.splitLines(cmdOut.getResult()), cmdOut.getExitValue());
	}
	
	protected abstract String[] getLinesToFeed();
	protected abstract String getCommandString();

}
