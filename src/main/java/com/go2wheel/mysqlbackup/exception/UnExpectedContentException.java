package com.go2wheel.mysqlbackup.exception;

public class UnExpectedContentException extends HasErrorIdAndMsgkey {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public UnExpectedContentException(String errorId, String msgkey, String unexpectedContent, Object...messagePlaceHolders) {
		super(errorId, msgkey, unexpectedContent, messagePlaceHolders);
	}
}
