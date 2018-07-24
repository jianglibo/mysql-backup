package com.go2wheel.mysqlbackup.annotation;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Target;

@Target({ TYPE, PARAMETER })
public @interface MetaAnno {
	
	String value() default "";

}
