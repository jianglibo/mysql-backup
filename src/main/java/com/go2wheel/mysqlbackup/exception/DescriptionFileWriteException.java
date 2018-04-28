package com.go2wheel.mysqlbackup.exception;

import java.nio.file.Path;

import com.go2wheel.mysqlbackup.value.Box;

public class DescriptionFileWriteException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public DescriptionFileWriteException(Box box, Path dstFile) {
		super(String.format("Write server %s's description.yml to %s failed.", box.getHost(), dstFile.toAbsolutePath().toString()));
	}

}
