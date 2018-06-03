package com.go2wheel.mysqlbackup.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.quartz.CronExpression;

import com.go2wheel.mysqlbackup.util.StringUtil;

public class CronExpressionValidator implements ConstraintValidator<CronExpressionConstraint, String> {
	
	private CronExpressionConstraint constraintAnnotation;
	
	
	@Override
	public void initialize(CronExpressionConstraint constraintAnnotation) {
		ConstraintValidator.super.initialize(constraintAnnotation);
		this.constraintAnnotation = constraintAnnotation;
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		try {
			if (constraintAnnotation.allowEmpty() && !StringUtil.hasAnyNonBlankWord(value)) {
				return true;
			} else if(!StringUtil.hasAnyNonBlankWord(value)) {
				return false;
			} else {
				new CronExpression(value);
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
