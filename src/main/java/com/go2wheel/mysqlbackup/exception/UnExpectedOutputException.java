package com.go2wheel.mysqlbackup.exception;

/**
 * use for when getting unexpected result.
 * @author Administrator
 *
 */
public class UnExpectedOutputException extends HasErrorIdAndMsgkey {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public UnExpectedOutputException(String errorId, String msgkey, String unexpectedContent, Object...messagePlaceHolders) {
		super(errorId, msgkey, unexpectedContent, messagePlaceHolders);
	}
}
