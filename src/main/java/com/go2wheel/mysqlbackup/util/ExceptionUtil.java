package com.go2wheel.mysqlbackup.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;

public class ExceptionUtil {
	
	public static void logErrorException(Logger logger, Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		logger.error(sw.toString());
	}

}
