package com.go2wheel.mysqlbackup.exception;

import java.util.regex.Pattern;


public class RemoteFileNotAbsoluteException extends RuntimeException {
	
	private static Pattern winPtn = Pattern.compile("^[a-zA-Z]:(/|\\\\).*");

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public RemoteFileNotAbsoluteException(String fn) {
		super("As a rule, remote file must be absolute, but '" + fn + "' wasn't!");
	}
	
	public static void throwIfNeed(String fn) {
		if (!fn.startsWith("/")) {
			if (!winPtn.matcher(fn).matches()) {
				throw new RemoteFileNotAbsoluteException(fn);
			}
		}
	}
}
