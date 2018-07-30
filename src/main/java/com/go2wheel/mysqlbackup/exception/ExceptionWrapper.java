package com.go2wheel.mysqlbackup.exception;

public class ExceptionWrapper extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Exception exception;
	
	public ExceptionWrapper(Exception exception) {
		this.setException(exception);
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

}
