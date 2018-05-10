package com.go2wheel.mysqlbackup.exception;

public class ScpException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ScpException(String lfile, String rfile, String description) {
		super(String.format("scp %s -> %s failed. %s", lfile, rfile, description));
	}

}
