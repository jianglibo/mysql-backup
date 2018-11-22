package com.go2wheel.mysqlbackup.exception;

public class AppConfigurationError extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String propertyName;
	
	public AppConfigurationError(String propertyName, String value) {
		super(propertyName + ": " + value);
		this.propertyName = propertyName;
	}
	

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	
	

}
