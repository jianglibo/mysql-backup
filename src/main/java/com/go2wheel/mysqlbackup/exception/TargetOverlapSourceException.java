package com.go2wheel.mysqlbackup.exception;

public class TargetOverlapSourceException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public TargetOverlapSourceException(String src, String dst) {
		super(String.format("Target folder overlaped with source folder. %s --> %s", src, dst));
	}

}
