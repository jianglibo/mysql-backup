package com.go2wheel.mysqlbackup.exception;

public class ExceptionWrapper extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Throwable exception;
	
	public ExceptionWrapper(Throwable exception) {
		this.setException(exception);
	}

	public Throwable getException() {
		return exception;
	}

	public void setException(Throwable exception) {
		this.exception = exception;
	}

}
