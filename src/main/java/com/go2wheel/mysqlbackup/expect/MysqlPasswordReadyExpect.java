package com.go2wheel.mysqlbackup.expect;

import static net.sf.expectit.matcher.Matchers.contains;
import static net.sf.expectit.matcher.Matchers.times;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.go2wheel.mysqlbackup.ApplicationState;
import com.go2wheel.mysqlbackup.exception.ExceptionWrapper;
import com.go2wheel.mysqlbackup.exception.UnExpectedContentException;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.util.ExpectitUtil;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import net.sf.expectit.Expect;
import net.sf.expectit.ExpectIOException;

public abstract class MysqlPasswordReadyExpect {
	
	public static final String BASH_PROMPT = "]#";

	protected final Session session;
	protected final Server server;
	protected Expect expect;
	
	public MysqlPasswordReadyExpect(Session session, Server server) {
		this.session = session;
		this.server = server;
	}
	
	private Channel getConnectedChannel() {
		Channel channel;
		try {
			channel = session.openChannel("shell");
			channel.connect();
		} catch (JSchException e) {
			throw new ExceptionWrapper(e);
		}
		return channel;
	}
	
	
	public List<String> start() {
		Channel channel = getConnectedChannel();
		// @formatter:off
		try {
			expect = ExpectitUtil.getExpectBuilder(channel, !ApplicationState.IS_PROD_MODE).build();			
			tillPasswordRequired();
			expect.expect(contains("password: "));
			expect.sendLine(server.getMysqlInstance().getPassword());
			return afterLogin();
		} catch (ExpectIOException e) {
			throw new UnExpectedContentException(null, "mysql.expect.password", e.getInputBuffer());
		} catch (IOException e) {
			throw new ExceptionWrapper(e);
		} finally {
			try {
				expect.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			channel.disconnect();
			expect = null;
		}
	}
	
	protected abstract void tillPasswordRequired() throws IOException;
	
	protected String expectBashPromptAndReturnRaw(int num) throws IOException {
		return expect.expect(times(num, contains(BASH_PROMPT))).getBefore();
	}
	
	protected String expectBashPromptAndReturnRaw(int num, long duration, TimeUnit tu) throws IOException {
		return expect.withTimeout(duration, tu).expect(times(num, contains(BASH_PROMPT))).getBefore();
	}

	
	protected List<String> expectBashPromptAndReturnList() throws IOException {
		return StringUtil.splitLines(expect.expect(contains(BASH_PROMPT)).getBefore()).stream().filter(s -> !s.trim().isEmpty()).collect(Collectors.toList());
	}
	
	/**
	 * May hold multiple command output.  
	 * @return
	 * @throws IOException
	 */
	protected abstract List<String> afterLogin() throws IOException;
}
