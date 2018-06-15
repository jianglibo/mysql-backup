package com.go2wheel.mysqlbackup.expect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.go2wheel.mysqlbackup.model.MysqlInstance;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.util.StringUtil;
import com.go2wheel.mysqlbackup.value.LinuxLsl;
import com.jcraft.jsch.Session;

public class MysqlDumpExpect extends MysqlPasswordReadyExpect {

	
	public MysqlDumpExpect(Session session, Server server) {
		super(session, server);
	}

	@Override
	protected List<String> afterLogin() throws IOException {
		expectBashPromptAndReturnRaw(1, 1, TimeUnit.DAYS);
		MysqlInstance mi = server.getMysqlInstance();
		expect.sendLine("ls -l " + mi.getDumpFileName());
		String s = expectBashPromptAndReturnRaw(1);
		List<String> r = new ArrayList<>();
		if (s.indexOf("cannot access") == -1) {
			Optional<String> found = StringUtil.splitLines(s).stream().filter(line -> LinuxLsl.matchAndReturnLinuxLsl(line).isPresent()).findFirst();
			if (found.isPresent()) {
				expect.sendLine(String.format("md5sum %s", mi.getDumpFileName()));
				r.add(found.get());
				String md5r = expectBashPromptAndReturnRaw(1, 1, TimeUnit.DAYS);
//				 md5sum /tmp/mysqldump.sql
//				 64f299244a31dd673466fd990b97c8d3  /tmp/mysqldump.sql
//				 [root@localhost ~
				Optional<String> md5Op = StringUtil.splitLines(md5r).stream().map(l -> l.trim().split("\\s+")).filter(ss -> ss.length == 2).map(ss -> ss[0].trim()).filter(ones -> ones.length() == 32).findFirst();
				if (md5Op.isPresent()) {
					r.add(md5Op.get());
				} else {
					r.add("");
				}
			}
		}
		return r;
	}
	
	private String getCmd() {
		MysqlInstance mi = server.getMysqlInstance();
		String cmd = "%smysqldump --max_allowed_packet=512M -u%s -p --quick --events --all-databases --flush-logs --delete-master-logs --single-transaction > %s";
		cmd = String.format(cmd, mi.getClientBin() == null ? "" : mi.getClientBin(), mi.getUsername(), mi.getDumpFileName());
		return cmd;
	}


	@Override
	protected void tillPasswordRequired() throws IOException {
		String cmd = getCmd();
		System.out.println(cmd);
		expect.sendLine(cmd);
	}
}
