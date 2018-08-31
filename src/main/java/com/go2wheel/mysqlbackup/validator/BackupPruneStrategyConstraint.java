package com.go2wheel.mysqlbackup.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = BackupPruneStrategyValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface BackupPruneStrategyConstraint {
	boolean allowEmpty() default false;
    String message() default "Invalid prune strategy. It must be a space seperated digital string, has 7 columns.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
