package com.go2wheel.mysqlbackup.exception;

import java.nio.file.Path;

public class AtomicWriteFileException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Path dstFile;
	
	public AtomicWriteFileException(Path dstFile) {
		super(String.format("Write to file: '%s' failed.", dstFile.toString()));
	}

	public Path getDstFile() {
		return dstFile;
	}

	public void setDstFile(Path dstFile) {
		this.dstFile = dstFile;
	}

}
