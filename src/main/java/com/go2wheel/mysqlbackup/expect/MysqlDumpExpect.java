package com.go2wheel.mysqlbackup.expect;

import java.io.IOException;
import java.util.Optional;

import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.util.StringUtil.LinuxFileInfo;
import com.go2wheel.mysqlbackup.value.Box;
import com.jcraft.jsch.Session;

public class MysqlDumpExpect extends MysqlPasswordReadyExpect<Optional<LinuxFileInfo>> {

	public static final String DUMP_FILE = "/tmp/mysqldump.sql";
	
	public MysqlDumpExpect(Session session, Box box) {
		super(session, box);
	}

//	@Override
	protected String getCmd() {
		String cmd = "mysqldump -u%s -p --quick --events --all-databases --flush-logs --delete-master-logs --single-transaction > %s";
		cmd = String.format(cmd, StringUtil.notEmptyValue(box.getMysqlInstance().getUsername()).orElse("root"), DUMP_FILE);
		return cmd;
	}


	@Override
	protected Optional<LinuxFileInfo> afterLogin() throws IOException {
		expectBashPromptAndReturnRaw(1);
		expect.sendLine("ls -l " + DUMP_FILE);
		String s = expectBashPromptAndReturnRaw(1);
		if (s.indexOf("cannot access") != -1) {
			return Optional.empty();
		} else {
			Optional<LinuxFileInfo> lff = StringUtil.splitLines(s).stream().map(StringUtil::matchLinuxLsl).filter(op -> op.isPresent()).findFirst().orElse(Optional.empty());
			if (lff.isPresent()) {
				expect.sendLine(String.format("md5sum %s", DUMP_FILE));
				s = expectBashPromptAndReturnRaw(1);
				lff.get().setMd5ByMd5sumOutput(s);
			}
			return lff;
		}
	}

	@Override
	protected void tillPasswordRequired() throws IOException {
		String cmd = "mysqldump -u%s -p --quick --events --all-databases --flush-logs --delete-master-logs --single-transaction > %s";
		cmd = String.format(cmd, StringUtil.notEmptyValue(box.getMysqlInstance().getUsername()).orElse("root"), DUMP_FILE);
		expect.sendLine(cmd);
	}
}
