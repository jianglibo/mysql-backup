package com.go2wheel.mysqlbackup.exception;

public class ScpToException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ScpToException(String lfile, String rfile, String description) {
		super(String.format("scp %s -> %s failed. %s", lfile, rfile, description));
	}

}
