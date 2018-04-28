package com.go2wheel.mysqlbackup.expect;

import static net.sf.expectit.matcher.Matchers.contains;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

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
	
	
	public T start() throws JSchException, IOException {
		Channel channel = session.openChannel("shell");
		channel.connect();

		// @formatter:off
		expect = new ExpectBuilder()
				.withOutput(channel.getOutputStream())
				.withInputs(channel.getInputStream(), channel.getExtInputStream())
				.withEchoOutput(System.out)
				.withEchoInput(System.err)
				.withExceptionOnFailure().build();
		try {
			expect.sendLine(getCmd());
			expect.expect(contains("password: "));
			expect.sendLine(box.getMysqlInstance().getPassword());
			return afterLogin();
		} finally {
			expect.close();
			channel.disconnect();
			expect = null;
		}
	}
	
	protected String expectBashPromptAndReturnRaw() throws IOException {
		return expect.expect(contains(BASH_PROMPT)).getBefore();
	}
	
	protected List<String> expectBashPromptAndReturnList() throws IOException {
		return StringUtil.splitLines(expect.expect(contains(BASH_PROMPT)).getBefore()).stream().filter(s -> !s.trim().isEmpty()).collect(Collectors.toList());
	}
	
	protected abstract String getCmd();

	protected abstract T afterLogin() throws IOException;
}
