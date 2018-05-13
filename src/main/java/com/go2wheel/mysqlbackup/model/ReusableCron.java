package com.go2wheel.mysqlbackup.model;

import com.go2wheel.mysqlbackup.validator.CronExpressionConstraint;

public class ReusableCron extends BaseModel {

	@CronExpressionConstraint
	private String expression;
	
	private String description;
	
	@Override
	public String toString() {
		return String.format("[%s, %s]", expression, description);
	}
	
	public ReusableCron() {
	}
	
	public ReusableCron(String expression, String description) {
		this.expression = expression;
		this.description = description;
	}
	

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
