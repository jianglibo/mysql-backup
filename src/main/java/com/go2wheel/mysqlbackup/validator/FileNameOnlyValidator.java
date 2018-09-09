package com.go2wheel.mysqlbackup.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.go2wheel.mysqlbackup.util.StringUtil;

public class FileNameOnlyValidator implements ConstraintValidator<FileNameOnlyConstraint, String> {
	
	private FileNameOnlyConstraint constraintAnnotation;
	
	
	@Override
	public void initialize(FileNameOnlyConstraint constraintAnnotation) {
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
				return value.indexOf('/') == -1 && value.indexOf('\\') == -1;
			}
		} catch (Exception e) {
			return false;
		}
	}

}
