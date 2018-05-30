package com.go2wheel.mysqlbackup.mail;

import java.util.List;

import com.go2wheel.mysqlbackup.model.BorgDownload;
import com.go2wheel.mysqlbackup.model.Diskfree;
import com.go2wheel.mysqlbackup.model.JobError;
import com.go2wheel.mysqlbackup.model.MysqlDump;
import com.go2wheel.mysqlbackup.model.MysqlFlush;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.UpTime;
import com.go2wheel.mysqlbackup.value.Box;

public class ServerContext {
	
	
	private List<UpTime> upTimes;
	private final List<MysqlFlush> mysqlFlushs;
	private final List<Diskfree> diskfrees;
	private final List<JobError> jobErrors;
	private final List<MysqlDump> mysqlDumps;
	private final List<BorgDownload> borgDownloads;
	
	private Box box;
	
	private Server server;
	
	public ServerContext(List<UpTime> upTimes, List<MysqlFlush> mysqlFlushs, List<Diskfree> diskfrees,
			List<JobError> jobErrors, List<MysqlDump> mysqlDumps, List<BorgDownload> borgDownloads) {
		super();
		this.upTimes = upTimes;
		this.mysqlFlushs = mysqlFlushs;
		this.diskfrees = diskfrees;
		this.jobErrors = jobErrors;
		this.mysqlDumps = mysqlDumps;
		this.borgDownloads = borgDownloads;
	}

	public List<UpTime> getUpTimes() {
		return upTimes;
	}

	public void setUpTimes(List<UpTime> upTimes) {
		this.upTimes = upTimes;
	}

	public List<MysqlFlush> getMysqlFlushs() {
		return mysqlFlushs;
	}

	public List<Diskfree> getDiskfrees() {
		return diskfrees;
	}

	public List<JobError> getJobErrors() {
		return jobErrors;
	}

	public List<MysqlDump> getMysqlDumps() {
		return mysqlDumps;
	}

	public List<BorgDownload> getBorgDownloads() {
		return borgDownloads;
	}

	public Box getBox() {
		return box;
	}

	public void setBox(Box box) {
		this.box = box;
	}

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}
}
