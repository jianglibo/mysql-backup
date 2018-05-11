package com.go2wheel.mysqlbackup.model;

public class ReusableCron extends BaseModel {

	private String expression;
	
	private String description;

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
