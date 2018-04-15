package com.go2wheel.mysqlbackup.exception;

public class SrcFolderNotExistException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public SrcFolderNotExistException(String fn) {
		super("Source project " + fn + " doesn't exists!");
	}

}
