package com.go2wheel.mysqlbackup.exception;

import com.go2wheel.mysqlbackup.value.ConfigFile;

public class NoActionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public NoActionException(ConfigFile configFile, String action) {
		super(configFile.getMypath() + ", does'nt has cmdtask: " + action);
	}

}
