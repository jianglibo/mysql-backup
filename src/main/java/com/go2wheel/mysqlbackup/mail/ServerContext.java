package com.go2wheel.mysqlbackup.mail;

import java.util.List;

import com.go2wheel.mysqlbackup.model.BorgDownload;
import com.go2wheel.mysqlbackup.model.Diskfree;
import com.go2wheel.mysqlbackup.model.JobError;
import com.go2wheel.mysqlbackup.model.MysqlDump;
import com.go2wheel.mysqlbackup.model.MysqlFlush;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.UpTime;

public class ServerContext {
	
	private List<UpTime> upTimes;
	private List<MysqlFlush> mysqlFlushs;
	private List<Diskfree> diskfrees;
	private List<JobError> jobErrors;
	private List<MysqlDump> mysqlDumps;
	private List<BorgDownload> borgDownloads;
	
	private Server server;
	
	public ServerContext() {}
	
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


	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public void setMysqlFlushs(List<MysqlFlush> mysqlFlushs) {
		this.mysqlFlushs = mysqlFlushs;
	}

	public void setDiskfrees(List<Diskfree> diskfrees) {
		this.diskfrees = diskfrees;
	}

	public void setJobErrors(List<JobError> jobErrors) {
		this.jobErrors = jobErrors;
	}

	public void setMysqlDumps(List<MysqlDump> mysqlDumps) {
		this.mysqlDumps = mysqlDumps;
	}

	public void setBorgDownloads(List<BorgDownload> borgDownloads) {
		this.borgDownloads = borgDownloads;
	}
	
	
}
