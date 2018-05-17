package com.go2wheel.mysqlbackup.exception;

public class ServerConnectionException extends ShowToUserException {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ServerConnectionException(String messageKey, String message) {
		super(messageKey, message);
	}
}
