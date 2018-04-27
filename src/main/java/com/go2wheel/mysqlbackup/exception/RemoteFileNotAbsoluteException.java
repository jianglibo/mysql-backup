package com.go2wheel.mysqlbackup.exception;

public class RemoteFileNotAbsoluteException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public RemoteFileNotAbsoluteException(String fn) {
		super("As a rule, remote file must started with slash /, but '" + fn + "' wasn't!");
	}
	
	
	public static void throwIfNeed(String fn) {
		if (!fn.startsWith("/")) {
			throw new RemoteFileNotAbsoluteException(fn);
		}
	}

}
