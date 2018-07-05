package com.go2wheel.mysqlbackup.annotation;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 
 * 
 * @author jianglibo@gmail.com
 *
 */
@Target({PARAMETER})
@Retention(RUNTIME)
public @interface CandidatesFromSQL {
	String value();
}
