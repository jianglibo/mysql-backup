package com.go2wheel.mysqlbackup.exception;

public class MyCommonException extends RuntimeException {
	
	private String reason;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public MyCommonException(String reason, String msg) {
		super(msg);
		this.reason = reason;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

}
