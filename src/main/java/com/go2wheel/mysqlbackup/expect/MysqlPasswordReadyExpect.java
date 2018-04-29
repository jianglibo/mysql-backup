package com.go2wheel.mysqlbackup.expect;

import static net.sf.expectit.matcher.Matchers.contains;
import static net.sf.expectit.matcher.Matchers.times;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.go2wheel.mysqlbackup.exception.IOExceptionWrapper;
import com.go2wheel.mysqlbackup.exception.JSchExceptionWrapper;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.value.Box;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import net.sf.expectit.Expect;
import net.sf.expectit.ExpectBuilder;

public abstract class MysqlPasswordReadyExpect<T> {
	
	public static final String BASH_PROMPT = "]#";

	protected final Session session;
	protected final Box box;
	protected Expect expect;
	
	public MysqlPasswordReadyExpect(Session session, Box box) {
		this.session = session;
		this.box = box;
	}
	
	private Channel getConnectedChannel() {
		Channel channel;
		try {
			channel = session.openChannel("shell");
			channel.connect();
		} catch (JSchException e) {
			throw new JSchExceptionWrapper(e);
		}
		return channel;
	}
	
	
	public T start() {
		Channel channel = getConnectedChannel();
		// @formatter:off
		try {
			expect = new ExpectBuilder()
					.withOutput(channel.getOutputStream())
					.withInputs(channel.getInputStream(), channel.getExtInputStream())
					.withEchoOutput(System.out)
					.withEchoInput(System.err)
					.withExceptionOnFailure().build();			
				tillPasswordRequired();
				expect.expect(contains("password: "));
				expect.sendLine(box.getMysqlInstance().getPassword());
				return afterLogin();
			} catch (IOException e) {
				throw new IOExceptionWrapper(e);
		} finally {
			try {
				expect.close();
			} catch (IOException e) {
			}
			channel.disconnect();
			expect = null;
		}
	}
	
	protected abstract void tillPasswordRequired() throws IOException;
	
	protected String expectBashPromptAndReturnRaw(int num) throws IOException {
		return expect.expect(times(num, contains(BASH_PROMPT))).getBefore();
	}
	
	protected List<String> expectBashPromptAndReturnList() throws IOException {
		return StringUtil.splitLines(expect.expect(contains(BASH_PROMPT)).getBefore()).stream().filter(s -> !s.trim().isEmpty()).collect(Collectors.toList());
	}
	
	protected abstract T afterLogin() throws IOException;
}
