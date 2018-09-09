package com.go2wheel.mysqlbackup.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = FileNameOnlyValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface FileNameOnlyConstraint {
	boolean allowEmpty() default false;
    String message() default "validate.file-name-only";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
