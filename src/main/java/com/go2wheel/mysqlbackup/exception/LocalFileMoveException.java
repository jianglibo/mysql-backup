package com.go2wheel.mysqlbackup.exception;

import java.nio.file.Path;
import java.util.List;

public class LocalFileMoveException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private List<Path> unrecoverFiles;
	
	public LocalFileMoveException(List<Path> unrecoverFiles) {
		super("batch move file failed.");
		this.setUnrecoverFiles(unrecoverFiles);
	}

	public List<Path> getUnrecoverFiles() {
		return unrecoverFiles;
	}

	public void setUnrecoverFiles(List<Path> unrecoverFiles) {
		this.unrecoverFiles = unrecoverFiles;
	}

}
