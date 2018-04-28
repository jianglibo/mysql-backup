package com.go2wheel.mysqlbackup.exception;

public class MysqlWrongPasswordException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public MysqlWrongPasswordException(String host) {
		super(String.format("Server %s has wrong mysql password.", host));
	}

}
