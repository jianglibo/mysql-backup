package com.go2wheel.mysqlbackup.event;

import org.springframework.context.ApplicationEvent;

import com.go2wheel.mysqlbackup.value.Box;

public class ServerCreateEvent extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Box box;

	public ServerCreateEvent(Object source, Box box) {
		super(source);
		this.setBox(box);
	}

	public Box getBox() {
		return box;
	}

	public void setBox(Box box) {
		this.box = box;
	}
	

}
