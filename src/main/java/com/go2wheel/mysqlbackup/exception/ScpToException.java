package com.go2wheel.mysqlbackup.exception;

public class ScpToException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ScpToException(String lfile, String rfile) {
		super(String.format("scp %s -> %s failed.", lfile, rfile));
	}

}
