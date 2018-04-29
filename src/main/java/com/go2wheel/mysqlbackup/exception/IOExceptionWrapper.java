package com.go2wheel.mysqlbackup.exception;

import java.io.IOException;

public class IOExceptionWrapper extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private IOException ioException;
	
	public IOExceptionWrapper(IOException ioException) {
		this.setIoException(ioException);
	}

	public IOException getIoException() {
		return ioException;
	}

	public void setIoException(IOException ioException) {
		this.ioException = ioException;
	}

}
