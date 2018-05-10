package com.go2wheel.mysqlbackup.exception;

public class NoServerSelectedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public NoServerSelectedException(String fn) {
		super("Copy target " + fn + " already exists!");
	}

}
