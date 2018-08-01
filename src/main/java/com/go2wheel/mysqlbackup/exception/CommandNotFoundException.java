package com.go2wheel.mysqlbackup.exception;

/**
 * It happen often.
 * 
 * @author jianglibo@gmail.com
 *
 */
public class CommandNotFoundException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String command;
	
	public CommandNotFoundException(String command) {
		super(String.format("command: '%s' not found.", command));
		this.setCommand(command);
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

}
