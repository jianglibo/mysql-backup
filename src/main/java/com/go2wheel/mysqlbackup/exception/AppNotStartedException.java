package com.go2wheel.mysqlbackup.exception;

public class AppNotStartedException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String app;
	
	public AppNotStartedException(String app) {
		super(app + "is not running.");
		this.setApp(app);
	}

	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = app;
	}

}
