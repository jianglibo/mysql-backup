package com.go2wheel.mysqlbackup.expect;

import static net.sf.expectit.matcher.Matchers.contains;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.go2wheel.mysqlbackup.ApplicationState;
import com.go2wheel.mysqlbackup.exception.AppNotStartedException;
import com.go2wheel.mysqlbackup.exception.MysqlAccessDeniedException;
import com.go2wheel.mysqlbackup.exception.UnExpectedInputException;
import com.go2wheel.mysqlbackup.model.MysqlInstance;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.util.ExpectitUtil;
import com.go2wheel.mysqlbackup.util.MysqlUtil;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.google.common.base.Charsets;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import net.sf.expectit.Expect;
import net.sf.expectit.ExpectBuilder;
import net.sf.expectit.ExpectIOException;

public abstract class MysqlInteractiveExpect<T> {

	private Session session;
	protected Expect expect;

	public MysqlInteractiveExpect(Session session) {
		this.session = session;
	}

	public T start(Server server) throws JSchException, IOException, MysqlAccessDeniedException, AppNotStartedException, UnExpectedInputException {
		return start(server.getMysqlInstance());
	}
	
	public T start(MysqlInstance mysqlInstance) throws UnExpectedInputException, JSchException, IOException, MysqlAccessDeniedException, AppNotStartedException {
		String user = mysqlInstance.getUsername("root");
		String password = mysqlInstance.getPassword();
		return start(mysqlInstance, user, password);
	}

	public T start(MysqlInstance mysqlInstance, String user, String password)
			throws JSchException, IOException, MysqlAccessDeniedException, AppNotStartedException, UnExpectedInputException {
		
		String clientExec = StringUtil.hasAnyNonBlankWord(mysqlInstance.getClientBin()) ? mysqlInstance.getClientBin() : "mysql";
		
		Channel channel = session.openChannel("shell");
		channel.connect();

		// @formatter:off
		ExpectBuilder eb = ExpectitUtil.getExpectBuilder(channel, false);
//		eb.withCharset(Charset.forName("UTF-8"));
		expect = eb.build();
		try {
			String cmd = String.format("%s -u%s -p", clientExec, user);
			expect.sendLine(cmd);
			expect.withTimeout(60, TimeUnit.SECONDS);
			expect.expect(contains("password: "));
			expect.sendLine(password);
			expect.expect(contains(MysqlUtil.MYSQL_PROMPT));
			return afterLogin();
		}catch (ExpectIOException e) {
			String ib = e.getInputBuffer();
			if (ib.contains("Access denied")) {
				throw new MysqlAccessDeniedException();
			}
			if (ib.contains("ERROR 2002")) {
				throw new AppNotStartedException("MYSQL");
			}
			
			if (ib.contains("CommandNotFoundException")) {
				throw new UnExpectedInputException("1000", "application.notconfiguraetd.mysqlclinet", "Cannot find mysql client.");
			}
			
			throw e;
		} finally {
			expect.close();
			channel.disconnect();
			expect = null;
		}
	}
	
	protected String expectMysqlPromptAndReturnRaw() throws IOException {
		return expect.expect(contains(MysqlUtil.MYSQL_PROMPT)).getBefore();
	}
	
	protected List<String> expectMysqlPromptAndReturnList() throws IOException {
		return StringUtil.splitLines(expect.withTimeout(300, TimeUnit.SECONDS)
				.expect(contains(MysqlUtil.MYSQL_PROMPT))
				.getBefore())
				.stream().filter(s -> !s.trim().isEmpty())
				.collect(Collectors.toList());
	}
	


	protected abstract T afterLogin() throws IOException;
}
