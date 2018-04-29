package com.go2wheel.mysqlbackup.exception;

import com.jcraft.jsch.JSchException;

public class JSchExceptionWrapper extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JSchException jSchException;
	
	public JSchExceptionWrapper(JSchException jSchException) {
		this.setjSchException(jSchException);
	}

	public JSchException getjSchException() {
		return jSchException;
	}

	public void setjSchException(JSchException jSchException) {
		this.jSchException = jSchException;
	}

}
