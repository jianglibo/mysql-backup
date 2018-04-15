package com.go2wheel.mysqlbackup.exception;

public class NoIgnoreFileException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public NoIgnoreFileException() {
		super("There should exist a file named 'copyignore.txt' or '.gitignore' in the root of source folder.");
	}

}
