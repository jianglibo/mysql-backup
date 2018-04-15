package com.go2wheel.mysqlbackup.commands;

public class Tm3 {
	
	private int acode;
	
	private String description;
	
	private String third;

	public Tm3(int acode, String description, String third) {
		super();
		this.acode = acode;
		this.description = description;
		this.third = third;
	}

	public int getAcode() {
		return acode;
	}

	public void setAcode(int acode) {
		this.acode = acode;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getThird() {
		return third;
	}

	public void setThird(String third) {
		this.third = third;
	}

}
