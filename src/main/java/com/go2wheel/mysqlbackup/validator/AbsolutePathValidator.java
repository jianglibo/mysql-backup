package com.go2wheel.mysqlbackup.validator;

import java.nio.file.Paths;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.go2wheel.mysqlbackup.util.StringUtil;

public class AbsolutePathValidator implements ConstraintValidator<AbsolutePathConstraint, String> {
	
	private AbsolutePathConstraint constraintAnnotation;
	
	
	@Override
	public void initialize(AbsolutePathConstraint constraintAnnotation) {
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
				return Paths.get(value).isAbsolute();
			}
		} catch (Exception e) {
			return false;
		}
	}

}
