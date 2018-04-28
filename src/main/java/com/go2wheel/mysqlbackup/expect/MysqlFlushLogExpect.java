package com.go2wheel.mysqlbackup.expect;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.go2wheel.mysqlbackup.exception.MysqlWrongPasswordException;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.value.Box;
import com.jcraft.jsch.Session;

/**
 * Be careful, when first login, the linux sever login message may appear.
 * Last login: Sat Apr 28 01:03:44 2018 from 192.168.33.1
   [root@localhost ~]# cat /var/lib/mysql/hm-log-bin.index
	cat /var/lib/mysql/hm-log-bin.index
 * @author admin
 *
 */
public class MysqlFlushLogExpect extends MysqlPasswordReadyExpect<Boolean> {
	
	private List<String> bf;

	public MysqlFlushLogExpect(Session session, Box box) {
		super(session, box);
	}


	protected String getCmd() {
		String cmd = "cat " + box.getMysqlInstance().getLogBinSetting().getLogBinIndex() + ";" + "mysqladmin -u%s -p flush-logs";
		cmd = String.format(cmd, StringUtil.notEmptyValue(box.getMysqlInstance().getUsername()).orElse("root"));
		return cmd;
	}

	@Override
	protected Boolean afterLogin() throws IOException {
		String s = expectBashPromptAndReturnRaw(1);
		if (s.indexOf("Access denied") != -1) {
			throw new MysqlWrongPasswordException(box.getHost());
		}
		return catIndex().size() == bf.size() + 1;
	}
	
	private List<String> catIndex() throws IOException {
		expect.sendLine("cat " + box.getMysqlInstance().getLogBinSetting().getLogBinIndex());
		String s = expectBashPromptAndReturnRaw(1);
		if (s.indexOf("Last login:") != -1) {
			s = expectBashPromptAndReturnRaw(1);
		}
		return StringUtil.splitLines(s).stream().map(l -> l.trim()).filter(l -> l.startsWith("./")).collect(Collectors.toList());
	}

	@Override
	protected void tillPasswordRequired() throws IOException {
		bf = catIndex();
		String cmd = "mysqladmin -u%s -p flush-logs";
		cmd = String.format(cmd, StringUtil.notEmptyValue(box.getMysqlInstance().getUsername()).orElse("root"));
		expect.sendLine(cmd);
	}
}
