package com.go2wheel.mysqlbackup.exception;

import com.go2wheel.mysqlbackup.model.Server;

public class MysqlDumpException extends RuntimeException {

	private String reason;
 
	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public MysqlDumpException(Server server, String reason) {
		super(String.format("Server %s's, %s.", server.getHost(), reason));
		this.reason = reason;
	}

}
