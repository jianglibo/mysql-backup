package com.go2wheel.mysqlbackup.util;

import java.io.IOException;
import java.util.Optional;

import com.go2wheel.mysqlbackup.util.StringUtil.LinuxLsl;
import com.go2wheel.mysqlbackup.value.Box;
import com.jcraft.jsch.Session;

public class MysqlDumpExpect extends MysqlPasswordReadyExpect<Optional<LinuxLsl>> {

	public static final String DUMP_FILE = "/tmp/mysqldump.sql";
	
	public MysqlDumpExpect(Session session, Box box) {
		super(session, box);
	}

	@Override
	protected String getCmd() {
		String cmd = "mysqldump -u%s -p --quick --events --all-databases --flush-logs --delete-master-logs --single-transaction > %s";
		cmd = String.format(cmd, StringUtil.notEmptyValue(box.getMysqlInstance().getUsername()).orElse("root"), DUMP_FILE);
		return cmd;
	}


	@Override
	protected Optional<LinuxLsl> afterLogin() throws IOException {
		expectBashPromptAndReturnRaw();
		expect.sendLine("ls -l " + DUMP_FILE);
		String s = expectBashPromptAndReturnRaw();
		if (s.indexOf("cannot access") != -1) {
			return Optional.empty();
		} else {
			return StringUtil.splitLines(s).stream().map(StringUtil::matchLinuxLsl).filter(op -> op.isPresent()).findFirst().orElse(Optional.empty());
		}
	}
}
