package com.go2wheel.mysqlbackup.exception;

public class ScpFromException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ScpFromException(String rfile, String lfile) {
		super(String.format("scp %s <- %s failed.", lfile, rfile));
	}

}
