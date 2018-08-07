package com.go2wheel.mysqlbackup.expect;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.go2wheel.mysqlbackup.model.Server;
import com.google.common.collect.Lists;
import com.jcraft.jsch.Session;

public class MysqlImportExpect extends MysqlPasswordReadyExpect {

	private String dumped;
	
	public MysqlImportExpect(Session session, Server server, String dumped) {
		super(session, server);
		this.dumped = dumped;
	}

	@Override
	protected List<String> afterLogin() throws IOException {
		String raw = expectBashPromptAndReturnRaw(1, 1, TimeUnit.DAYS);
		return Lists.newArrayList(raw.trim());
	}
	

	@Override
	protected void tillPasswordRequired() throws IOException {
		String cmd = String.format("mysql -uroot -p < %s", dumped);
		expect.sendLine(cmd);
	}
}
