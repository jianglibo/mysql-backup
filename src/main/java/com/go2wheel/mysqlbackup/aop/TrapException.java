package com.go2wheel.mysqlbackup.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.quartz.Job;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TrapException {
	Class<? extends Job> value();
}
