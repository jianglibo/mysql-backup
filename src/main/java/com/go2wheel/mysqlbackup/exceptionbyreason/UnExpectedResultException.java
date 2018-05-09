package com.go2wheel.mysqlbackup.exceptionbyreason;

public class UnExpectedResultException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public UnExpectedResultException(String unexcepted) {
		super(unexcepted);
	}

	public String getUnexcepted() {
		return getMessage();
	}


}
