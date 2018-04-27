package com.go2wheel.mysqlbackup.event;

import org.springframework.context.ApplicationEvent;

public class ServerChangeEvent extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ServerChangeEvent(Object source) {
		super(source);
	}
	

}
