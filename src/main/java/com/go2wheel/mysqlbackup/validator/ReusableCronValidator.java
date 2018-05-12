package com.go2wheel.mysqlbackup.validator;

import org.quartz.CronExpression;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.go2wheel.mysqlbackup.model.ReusableCron;

@Component
public class ReusableCronValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return ReusableCron.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		try {
			new CronExpression(((ReusableCron)target).getExpression());
		} catch (Exception e) {
			errors.reject("expression", "invalid cron expression.");
		}
	}

}
