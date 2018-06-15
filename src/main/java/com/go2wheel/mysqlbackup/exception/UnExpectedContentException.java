package com.go2wheel.mysqlbackup.exception;

public class UnExpectedContentException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String unexpectedContent;
	
	public UnExpectedContentException(String unexpectedContent) {
		super(unexpectedContent);
	}

	public String getUnexpectedContent() {
		return unexpectedContent;
	}

	public void setUnexpectedContent(String unexpectedContent) {
		this.unexpectedContent = unexpectedContent;
	}

}
