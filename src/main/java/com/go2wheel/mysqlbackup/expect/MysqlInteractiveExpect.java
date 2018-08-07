package com.go2wheel.mysqlbackup.expect;

import static net.sf.expectit.matcher.Matchers.contains;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.go2wheel.mysqlbackup.ApplicationState;
import com.go2wheel.mysqlbackup.exception.AppNotStartedException;
import com.go2wheel.mysqlbackup.exception.MysqlAccessDeniedException;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.util.ExpectitUtil;
import com.go2wheel.mysqlbackup.util.MysqlUtil;
import com.go2wheel.mysqlbackup.util.StringUtil;
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

	public T start(Server box) throws JSchException, IOException, MysqlAccessDeniedException, AppNotStartedException {
		return start(box.getMysqlInstance().getUsername("root"), box.getMysqlInstance().getPassword());
	}

	public T start(String user, String password)
			throws JSchException, IOException, MysqlAccessDeniedException, AppNotStartedException {
		Channel channel = session.openChannel("shell");
		channel.connect();

		// @formatter:off
		ExpectBuilder eb = ExpectitUtil.getExpectBuilder(channel, !ApplicationState.IS_PROD_MODE);
		expect = eb.build();
		try {
			String cmd = String.format("mysql -u%s -p", user);
			expect.sendLine(cmd);
			expect.expect(contains("password: "));
			expect.sendLine(password);
			
			try {
				expect.withTimeout(500, TimeUnit.MILLISECONDS).expect(contains("Access denied"));
				throw new MysqlAccessDeniedException();
			} catch (ExpectIOException e) {
			}
			
			try {
				expect.withTimeout(500, TimeUnit.MILLISECONDS).expect(contains("ERROR 2002"));
				throw new AppNotStartedException("MYSQL");
			} catch (ExpectIOException e) {
				
			}
			expect.expect(contains(MysqlUtil.MYSQL_PROMPT));
			return afterLogin();
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
		return StringUtil.splitLines(expect.expect(contains(MysqlUtil.MYSQL_PROMPT)).getBefore()).stream().filter(s -> !s.trim().isEmpty()).collect(Collectors.toList());
	}
	


	protected abstract T afterLogin();
}
