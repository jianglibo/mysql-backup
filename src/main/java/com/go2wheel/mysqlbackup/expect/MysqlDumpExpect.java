package com.go2wheel.mysqlbackup.expect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.go2wheel.mysqlbackup.util.MysqlUtil;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.value.Box;
import com.go2wheel.mysqlbackup.value.LinuxLsl;
import com.jcraft.jsch.Session;

public class MysqlDumpExpect extends MysqlPasswordReadyExpect {

	
	public MysqlDumpExpect(Session session, Box box) {
		super(session, box);
	}

	protected String getCmd() {
		String cmd = "mysqldump -u%s -p --quick --events --all-databases --flush-logs --delete-master-logs --single-transaction > %s";
		cmd = String.format(cmd, StringUtil.notEmptyValue(box.getMysqlInstance().getUsername()).orElse("root"), MysqlUtil.DUMP_FILE_NAME);
		return cmd;
	}


	@Override
	protected List<String> afterLogin() throws IOException {
		expectBashPromptAndReturnRaw(1);
		expect.sendLine("ls -l " + MysqlUtil.DUMP_FILE_NAME);
		String s = expectBashPromptAndReturnRaw(1);
		List<String> r = new ArrayList<>(Arrays.asList(s));
		if (s.indexOf("cannot access") == -1) {
			Optional<String> found = StringUtil.splitLines(s).stream().filter(line -> LinuxLsl.matchAndReturnLinuxLsl(line).isPresent()).findFirst();
			if (found.isPresent()) {
				expect.sendLine(String.format("md5sum %s", MysqlUtil.DUMP_FILE_NAME));
				r.set(0, found.get());
				r.add(expectBashPromptAndReturnRaw(1));
			}
		}
		return r;
	}

	@Override
	protected void tillPasswordRequired() throws IOException {
		String cmd = "mysqldump -u%s -p --quick --events --all-databases --flush-logs --delete-master-logs --single-transaction > %s";
		cmd = String.format(cmd, StringUtil.notEmptyValue(box.getMysqlInstance().getUsername()).orElse("root"), MysqlUtil.DUMP_FILE_NAME);
		expect.sendLine(cmd);
	}
}
