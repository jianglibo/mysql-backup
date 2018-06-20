package com.go2wheel.mysqlbackup.annotation;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import com.go2wheel.mysqlbackup.validator.PossibleValueValidator;

@Target(PARAMETER)
@Constraint(validatedBy = PossibleValueValidator.class)
@Retention(RUNTIME)
public @interface ShowPossibleValue {
	String[] value() default {};
	String message() default "Value was not in possible values.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
