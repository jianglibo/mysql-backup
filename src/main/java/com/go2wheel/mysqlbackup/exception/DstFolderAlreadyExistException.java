package com.go2wheel.mysqlbackup.exception;

public class DstFolderAlreadyExistException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public DstFolderAlreadyExistException(String fn) {
		super("Copy target " + fn + " already exists!");
	}

}
