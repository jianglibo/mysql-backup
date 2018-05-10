package com.go2wheel.mysqlbackup.exception;

public class EntityNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public EntityNotFoundException(Class<?> clazz, int id) {
		super(String.format("No %s entry found with id: %s", clazz.getName(), id));
	}

}
