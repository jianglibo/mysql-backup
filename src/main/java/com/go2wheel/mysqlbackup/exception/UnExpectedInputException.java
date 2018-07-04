package com.go2wheel.mysqlbackup.exception;

public class UnExpectedInputException extends HasErrorIdAndMsgkey {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	public UnExpectedInputException(String errorId, String msgkey, String unexpectedInputValue, Object...messagePlaceHolders) {
		super(errorId, msgkey, unexpectedInputValue, messagePlaceHolders);
	}

}
