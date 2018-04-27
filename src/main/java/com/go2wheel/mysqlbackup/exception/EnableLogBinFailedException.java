package com.go2wheel.mysqlbackup.exception;

public class EnableLogBinFailedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public EnableLogBinFailedException(String host) {
		super(String.format("Server %s enable mysql log_bin failed.", host));
	}

}
