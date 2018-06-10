package com.go2wheel.mysqlbackup.mail;

import java.util.List;

import com.go2wheel.mysqlbackup.model.BorgDownload;
import com.go2wheel.mysqlbackup.model.JobError;
import com.go2wheel.mysqlbackup.model.MysqlDump;
import com.go2wheel.mysqlbackup.model.MysqlFlush;
import com.go2wheel.mysqlbackup.model.Server;
import com.go2wheel.mysqlbackup.model.ServerState;
import com.go2wheel.mysqlbackup.model.StorageState;

public class ServerContext {
	
	private List<ServerState> serverStates;
	private List<MysqlFlush> mysqlFlushs;
	private List<StorageState> storageStates;
	
	private List<JobError> jobErrors;
	private List<MysqlDump> mysqlDumps;
	private List<BorgDownload> borgDownloads;
	
	private Server server;
	
	public ServerContext() {}
	
	public ServerContext(List<ServerState> serverStates, List<MysqlFlush> mysqlFlushs, List<StorageState> storageStates,
			List<JobError> jobErrors, List<MysqlDump> mysqlDumps, List<BorgDownload> borgDownloads) {
		super();
		this.mysqlFlushs = mysqlFlushs;
		this.jobErrors = jobErrors;
		this.mysqlDumps = mysqlDumps;
		this.borgDownloads = borgDownloads;
		this.serverStates = serverStates;
		this.storageStates = storageStates;
	}



	public List<MysqlFlush> getMysqlFlushs() {
		return mysqlFlushs;
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

	public List<ServerState> getServerStates() {
		return serverStates;
	}

	public void setServerStates(List<ServerState> serverStates) {
		this.serverStates = serverStates;
	}

	public List<StorageState> getStorageStates() {
		return storageStates;
	}

	public void setStorageStates(List<StorageState> storageStates) {
		this.storageStates = storageStates;
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
