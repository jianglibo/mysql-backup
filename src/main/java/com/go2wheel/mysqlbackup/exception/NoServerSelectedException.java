package com.go2wheel.mysqlbackup.exception;

public class NoServerSelectedException extends ShowToUserException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public NoServerSelectedException(String messageKey, String message) {
		super(messageKey, message);
	}
}
