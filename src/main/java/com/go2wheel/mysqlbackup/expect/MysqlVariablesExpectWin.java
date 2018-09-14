package com.go2wheel.mysqlbackup.expect;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.go2wheel.mysqlbackup.exception.MysqlAccessDeniedException;
import com.go2wheel.mysqlbackup.exception.MysqlWrongPasswordException;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.jcraft.jsch.Session;

public class MysqlVariablesExpectWin extends MysqlPasswordReadyExpect<Map<String, String>> {
	
	public static final String lineDecoration = ",,,"; 
	
	private String[] variables;

	public MysqlVariablesExpectWin(Session session, Server server, String...variables) {
		super(session, server);
		this.variables = variables;
	}

	@Override
	protected void invokeCommandWhichCausePasswordPrompt() throws IOException {
//		String[] ss = new String[] {"innodb_version", "protocol_version", "version", "version_comment", "version_compile_machine", "version_compile_os"};
		String clientBin = StringUtil.hasAnyNonBlankWord(server.getMysqlInstance().getClientBin()) ? server.getMysqlInstance().getClientBin() : "mysql"; 
		String joined = Stream.of(variables).map(it -> "'" + it + "'").collect(Collectors.joining(",", "@(", ")"));
		
		StringBuffer sb = new StringBuffer(clientBin)
				.append(" -uroot -p")
				.append(" -e 'show variables' | ")
				.append("Foreach-Object {$_.trim()} |")
				.append("Where-Object {$_} |")
				.append("Where-Object {")
				.append(joined)
				.append(" -contains ($_ -split \"\\s+\")[0] } | ForEach-Object -Begin {'---start---'} -Process {'").append(lineDecoration).append("' + $_ + '")
				.append(lineDecoration).append("'} -End {'---end---'}");
		
		expect.sendLine(sb.toString());
	}

	@Override
	protected Map<String, String> afterLogin() throws IOException, MysqlAccessDeniedException {
		String s = expectBashPromptAndReturnRaw(1);
		if (s.indexOf("Access denied") != -1) {
			throw new MysqlWrongPasswordException(server.getHost());
		}
		s = s.replaceAll("\\e\\[[\\d;]*[^\\d;]","").trim();
		
		return StringUtil.splitLines(s).stream().map(line -> line.trim())
				.filter(line -> line.startsWith(lineDecoration))
				.filter(line -> line.startsWith(lineDecoration))
				.map(line -> line.split(lineDecoration))
				.filter(lls -> lls.length >= 2)
				.map(lls -> lls[1])
				.map(line -> line.split("\\s+", 2))
				.map(lls -> {
					if (lls.length == 1) {
						return new String[] {lls[0], ""};
					} else {
						return lls;
					}
				})
				.collect(Collectors.toMap(lls -> lls[0], lls -> {
					String v = lls[1];
					v = v.replaceAll("\\\\\\\\", "\\\\");
					return v;
				}));
	}

}
