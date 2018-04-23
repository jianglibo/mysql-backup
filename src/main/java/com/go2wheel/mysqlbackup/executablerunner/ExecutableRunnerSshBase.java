package com.go2wheel.mysqlbackup.executablerunner;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.value.ExternalExecuteResult;
import com.go2wheel.mysqlbackup.value.MysqlInstance;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.transport.TransportException;

public abstract class ExecutableRunnerSshBase implements ExecutableRunnerSsh<List<String>> {
	
	protected SSHClient sshClient;
	
	protected ExternalExecuteResult<List<String>> prevResult;
	
	protected MysqlInstance instance;
	
	public ExecutableRunnerSshBase(SSHClient sshClient,MysqlInstance instance) {
		this.sshClient = sshClient;
		this.instance = instance;
		this.prevResult = null;
	}
	
	public ExecutableRunnerSshBase(SSHClient sshClient,MysqlInstance instance, ExternalExecuteResult<List<String>> prevResult) {
		this.sshClient = sshClient;
		this.prevResult = prevResult;
		this.instance = instance;
	}
	
	@Override
	public void setPrevResult(ExternalExecuteResult<List<String>> prevResult) {
		this.prevResult = prevResult;
	}

	@Override
	public ExternalExecuteResult<List<String>> execute() {
		String reason;
		try {
			final Session session = sshClient.startSession();
			try {
				ExternalExecuteResult<List<String>> er = executeInternal(session, instance);
				if (er.isSuccess()) {
					ExternalExecuteResult<List<String>> er1 = afterSuccessInvoke(er);
					return er1 == null ? er : er1;
				} else {
					return er;
				}
				
			} catch (IOException e) {
				reason = e.getMessage();
				e.printStackTrace();
			} finally {
				session.close();
			}
		} catch (ConnectionException | TransportException e) {
			reason = e.getMessage();
			e.printStackTrace();
		}
		return ExternalExecuteResult.failedResult(reason);
	}
	
	protected abstract ExternalExecuteResult<List<String>> afterSuccessInvoke(ExternalExecuteResult<List<String>> externalExecuteResult);
	
	protected ExternalExecuteResult<List<String>> executeInternal(Session session, MysqlInstance instance) throws ConnectionException, TransportException, IOException {
		if (getCommandString() == null || getCommandString().trim().isEmpty()) {
			return ExternalExecuteResult.failedResult("empty ssh command invoked.");
		}
		final Command cmd = session.exec(getCommandString());
		PrintWriter pw = getProcessWriter(cmd.getOutputStream());
		String[] linesToFeed = getLinesToFeed();
		if (linesToFeed != null && linesToFeed.length > 0) {
			for(String line: linesToFeed) {
				pw.println(line);
			}
		}
		String cmdOut = IOUtils.readFully(cmd.getInputStream()).toString();
		return new ExternalExecuteResult<List<String>>(StringUtil.splitLines(cmdOut), cmd.getExitStatus());
	}
	
	protected abstract String[] getLinesToFeed();
	protected abstract String getCommandString();

}
