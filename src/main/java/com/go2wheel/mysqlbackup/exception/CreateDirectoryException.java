package com.go2wheel.mysqlbackup.exception;

import java.io.IOException;

public class CreateDirectoryException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private IOException ioException;
	
	public CreateDirectoryException(IOException ioException) {
		super(ioException.getMessage());
		this.setIoException(ioException);
	}

	public IOException getIoException() {
		return ioException;
	}

	public void setIoException(IOException ioException) {
		this.ioException = ioException;
	}

}
