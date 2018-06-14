package com.go2wheel.mysqlbackup.annotation;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Instruction for commond object field provider.
 * 
 * @author jianglibo@gmail.com
 *
 */
@Target({PARAMETER, FIELD})
@Retention(RUNTIME)
public @interface DbTableName {
}
