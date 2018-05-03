package com.go2wheel.mysqlbackup.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;

public class ExceptionUtil {
	
	public static void logErrorException(Logger logger, Exception e) {
		PrintWriter sw = new PrintWriter(new StringWriter());
		e.printStackTrace(sw);
		logger.error(sw.toString());
	}

}
