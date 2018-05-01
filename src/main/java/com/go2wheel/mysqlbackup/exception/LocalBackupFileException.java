package com.go2wheel.mysqlbackup.exception;

public class LocalBackupFileException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String reason;
	
	public LocalBackupFileException(String reason) {
		super(reason);
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

}
