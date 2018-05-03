package com.go2wheel.mysqlbackup.exception;

public class RunRemoteCommandException extends Exception {
	
	
	private String command;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public RunRemoteCommandException(String command, String msg) {
		super(msg);
		this.command = command;
	}
	
	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}


}
