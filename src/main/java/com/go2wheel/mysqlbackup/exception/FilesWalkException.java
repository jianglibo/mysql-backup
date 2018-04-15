package com.go2wheel.mysqlbackup.exception;

public class FilesWalkException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public FilesWalkException(String msg) {
		super("While walking source project tree an error happen: " + msg);
	}

}
