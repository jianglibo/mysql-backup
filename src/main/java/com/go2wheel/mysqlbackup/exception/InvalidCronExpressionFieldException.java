package com.go2wheel.mysqlbackup.exception;

import com.go2wheel.mysqlbackup.job.CronExpressionBuilder.CronExpressionField;

public class InvalidCronExpressionFieldException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private CronExpressionField field;
	
	private String expression;
	
	public InvalidCronExpressionFieldException(CronExpressionField field, String expression) {
		super(String.format("invalid expression in %s field, : %s" , field, expression));
		this.field = field;
		this.expression = expression;
	}

	public CronExpressionField getField() {
		return field;
	}

	public void setField(CronExpressionField field) {
		this.field = field;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

}
