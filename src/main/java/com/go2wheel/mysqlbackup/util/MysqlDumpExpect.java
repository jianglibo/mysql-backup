package com.go2wheel.mysqlbackup.util;

import static net.sf.expectit.matcher.Matchers.contains;
import static net.sf.expectit.matcher.Matchers.regexp;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.go2wheel.mysqlbackup.value.Box;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import net.sf.expectit.Expect;
import net.sf.expectit.ExpectBuilder;
import net.sf.expectit.Result;

public abstract class MysqlDumpExpect<T> {

	private Session session;
	private Box box;
	protected Expect expect;
	
	public static final String DUMP_FILE = "/tmp/mysqldump.sql";
	
	public static class DumpResult {
		
	}
	
	public MysqlDumpExpect(Session session, Box box) {
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
			String cmd = "mysqldump -u%s -p --quick --events --all-databases --flush-logs --delete-master-logs --single-transaction > %s";
			cmd = String.format(cmd, StringUtil.notEmptyValue(box.getMysqlInstance().getUsername()).orElse("root"), DUMP_FILE);
			expect.sendLine(cmd);
			expect.expect(contains("password: "));
			expect.sendLine(box.getMysqlInstance().getPassword());
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
	
	protected String getColumnValue(List<String> lines, String key, int zeroBasedkeyColumn, int zeroBasedValueColumn) {
		int maxColumn = zeroBasedkeyColumn > zeroBasedValueColumn ? zeroBasedkeyColumn : zeroBasedValueColumn; 
		StringBuffer ptbuf = new StringBuffer("\\s*");
		for(int i = 0 ; i <= maxColumn; i++) {
			if (i == zeroBasedkeyColumn) {
				ptbuf.append("\\|\\s+" + key + "\\s+");
			} else if(i == zeroBasedValueColumn) {
				ptbuf.append("\\|\\s+([^\\s]+)\\s+");
			} else {
				ptbuf.append("\\|\\s+[^\\s]+\\s+");
			}
		}
		ptbuf.append(".*");
		Pattern ptn = Pattern.compile(ptbuf.toString());
		return lines.stream().map(line -> ptn.matcher(line)).filter(m -> m.matches()).map(m -> m.group(1)).findFirst().orElse("");
	}



	protected abstract T afterLogin();
}
