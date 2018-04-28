package com.go2wheel.mysqlbackup.exception;

import com.go2wheel.mysqlbackup.value.Box;

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
	
	public MysqlDumpException(Box box, String reason) {
		super(String.format("Server %s's, %s.", box.getHost(), reason));
		this.reason = reason;
	}

}
