package com.go2wheel.mysqlbackup.validator;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.go2wheel.mysqlbackup.util.StringUtil;

public class BackupPruneStrategyValidator implements ConstraintValidator<BackupPruneStrategyConstraint, String> {
	
	private BackupPruneStrategyConstraint backupPruneStragegyAnnotation;
	
	
	private Pattern ptn = Pattern.compile("^\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s*$");
	
	@Override
	public void initialize(BackupPruneStrategyConstraint constraintAnnotation) {
		ConstraintValidator.super.initialize(constraintAnnotation);
		this.backupPruneStragegyAnnotation = constraintAnnotation;
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (backupPruneStragegyAnnotation.allowEmpty() && !StringUtil.hasAnyNonBlankWord(value)) {
			return true;
		} else if(!StringUtil.hasAnyNonBlankWord(value)) {
			return false;
		} else {
			return ptn.matcher(value).matches();
		}
	}

}
