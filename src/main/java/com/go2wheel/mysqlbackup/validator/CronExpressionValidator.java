package com.go2wheel.mysqlbackup.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.quartz.CronExpression;

public class CronExpressionValidator implements
ConstraintValidator<CronExpressionConstraint, String>{

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		try {
			new CronExpression(value);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
