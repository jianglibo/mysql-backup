package com.go2wheel.mysqlbackup.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;

import com.go2wheel.mysqlbackup.value.RemoteCommandResult;

public class ExceptionUtil {
	
	public static void logErrorException(Logger logger, Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		logger.error(sw.toString());
	}
	
	public static void logRemoteCommandResult(Logger logger, RemoteCommandResult rcr) {
		String s = "";
		if (rcr != null) {
			s = String.join("\n", rcr.getAllTrimedNotEmptyLines());
		}
		
		logger.error("exitValue: {}, output: {}", rcr.getExitValue(), s);
	}

	public static String stackTraceToString(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}

}
