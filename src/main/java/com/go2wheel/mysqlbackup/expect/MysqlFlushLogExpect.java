package com.go2wheel.mysqlbackup.expect;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.value.Box;
import com.jcraft.jsch.Session;

public class MysqlFlushLogExpect extends MysqlPasswordReadyExpect<List<String>> {

	public MysqlFlushLogExpect(Session session, Box box) {
		super(session, box);
	}

	@Override
	protected String getCmd() {
		String cmd = "mysqladmin -u%s -p flush-logs";
		cmd = String.format(cmd, StringUtil.notEmptyValue(box.getMysqlInstance().getUsername()).orElse("root"));
		return cmd;
	}


	@Override
	protected List<String> afterLogin() throws IOException {
		expectBashPromptAndReturnRaw();
		expect.sendLine("cat " + box.getMysqlInstance().getLogBinSetting().getLogBinIndex());
		String s = expectBashPromptAndReturnRaw();
		return StringUtil.splitLines(s).stream().map(l -> l.trim()).filter(l -> l.startsWith("./")).collect(Collectors.toList());
	}
}
