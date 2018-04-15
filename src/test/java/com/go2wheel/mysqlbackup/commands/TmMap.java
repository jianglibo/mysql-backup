package com.go2wheel.mysqlbackup.commands;

public class TmMap {

	private int acode;
	
	private String map;

	public TmMap(int acode, String map) {
		super();
		this.acode = acode;
		this.map = map;
	}

	public int getAcode() {
		return acode;
	}

	public void setAcode(int acode) {
		this.acode = acode;
	}

	public String getMap() {
		return map;
	}

	public void setMap(String map) {
		this.map = map;
	}
	
	
}
