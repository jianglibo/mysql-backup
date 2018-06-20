package com.go2wheel.mysqlbackup.exception;

public class ShowToUserException extends HasErrorIdAndMsgkey {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	public ShowToUserException(String errorId, String msgkey, String message, Object...messagePlaceHolders) {
		super(errorId, msgkey, message, messagePlaceHolders);
	}

}
