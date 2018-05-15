package com.go2wheel.mysqlbackup.exception;

public class NoServerSelectedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final String messageKey;
	
	public NoServerSelectedException(String messageKey, String message) {
		super(message);
		this.messageKey = messageKey;
	}

	public String getMessageKey() {
		return messageKey;
	}

}
