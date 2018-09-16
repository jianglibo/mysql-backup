package com.go2wheel.mysqlbackup.expect;

import static net.sf.expectit.filter.Filters.removeColors;
import static net.sf.expectit.matcher.Matchers.contains;
import static net.sf.expectit.matcher.Matchers.times;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.go2wheel.mysqlbackup.ApplicationState;
import com.go2wheel.mysqlbackup.exception.ExceptionWrapper;
import com.go2wheel.mysqlbackup.exception.MysqlAccessDeniedException;
import com.go2wheel.mysqlbackup.exception.UnExpectedOutputException;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.util.ExpectitUtil;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.value.OsTypeWrapper;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import net.sf.expectit.Expect;
import net.sf.expectit.ExpectIOException;
import net.sf.expectit.Result;

public abstract class MysqlPasswordReadyExpect<T> {
	
	public static final String BASH_PROMPT = "]#";
	public static final String POWERSHELL_START = "---start---";
	public static final String POWERSHELL_END = "---end---";

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
//			((ChannelShell)channel).setPtyType("xterm");
//			((ChannelShell)channel).setPty(false);
			channel.connect();
		} catch (JSchException e) {
			throw new ExceptionWrapper(e);
		}
		return channel;
	}
	
	
	public T start() throws UnExpectedOutputException, MysqlAccessDeniedException {
		Channel channel = getConnectedChannel();
		// @formatter:off
		try {
			expect = ExpectitUtil.getExpectBuilder(channel, !ApplicationState.IS_PROD_MODE).withInputFilters(removeColors()).build();
			invokeCommandWhichCausePasswordPrompt();
			// if wrong command invoked, password prompt will never show up.
			
			boolean goon = true;
			
			try {
				Result rs = expect.withTimeout(3, TimeUnit.SECONDS).expect(contains("FullyQualifiedErrorId"));
				goon = false;
			} catch (Exception e) {
				e.printStackTrace();
//				throw new UnExpectedOutputException(null, "mysql.expect.password", e.getInputBuffer());
			}
			
			if (goon) {
				expect.expect(contains("password: "));
				expect.sendLine(server.getMysqlInstance().getPassword());
			}
			return afterLogin();
		} catch (ExpectIOException e) {
			throw new UnExpectedOutputException(null, "mysql.expect.password", e.getInputBuffer());
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
	
	protected abstract void invokeCommandWhichCausePasswordPrompt() throws IOException;
	
	protected Result expectBashPromptAndReturnRaw(int num) throws IOException {
		if (OsTypeWrapper.of(server.getOs()).isWin()) {
			return expect.expect(times(num, contains(POWERSHELL_END)));//.getBefore();
		} else {
			return expect.expect(times(num, contains(BASH_PROMPT)));//.getBefore();
		}
		
	}
	
	protected String expectBashPromptAndReturnRaw(int num, long duration, TimeUnit tu) throws IOException {
		if (OsTypeWrapper.of(server.getOs()).isWin()) {
			return expect.withTimeout(duration, tu).expect(times(num, contains(POWERSHELL_END))).getBefore();
		} else {
			return expect.withTimeout(duration, tu).expect(times(num, contains(BASH_PROMPT))).getBefore();
		}
	}

	protected void checkAccessDenied(List<String> s) throws MysqlAccessDeniedException {
		if (s.stream().anyMatch(line -> line.indexOf("Access denied for") != -1)) {
			throw new MysqlAccessDeniedException();
		}
	}
	
	protected List<String> expectBashPromptAndReturnList() throws IOException {
		if (OsTypeWrapper.of(server.getOs()).isWin()) {
			return StringUtil.splitLines(expect.expect(contains(POWERSHELL_END)).getBefore()).stream().filter(s -> !s.trim().isEmpty()).collect(Collectors.toList());
		} else {
			return StringUtil.splitLines(expect.expect(contains(BASH_PROMPT)).getBefore()).stream().filter(s -> !s.trim().isEmpty()).collect(Collectors.toList());
		}
	}
	
	/**
	 * May hold multiple command output.  
	 * @return
	 * @throws IOException
	 * @throws MysqlAccessDeniedException 
	 */
	protected abstract T afterLogin() throws IOException, MysqlAccessDeniedException;
}
