package com.go2wheel.mysqlbackup.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.springframework.dao.DuplicateKeyException;

import com.go2wheel.mysqlbackup.value.RemoteCommandResult;

public class ExceptionUtil {

	public static void logErrorException(Logger logger, Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		logger.error(sw.toString());
	}

	public static void logThrowable(Logger logger, Throwable e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		logger.error(sw.toString());
	}

	public static void logRemoteCommandResult(Logger logger, RemoteCommandResult rcr) {
		String s = "";
		if (rcr != null) {
			s = String.join("\n", rcr.getAllTrimedNotEmptyLines());
			logger.error("exitValue: {}, output: {}", rcr.getExitValue(), s);
		}
	}

	public static String stackTraceToString(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}

	public static String stackTraceToString(Throwable e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}

	public static String parseDuplicateException(DuplicateKeyException de) {
		 Pattern ptn = Pattern.compile(".*\\s+([^\\s]+)\\s+table:.*?$");
		 Matcher m = ptn.matcher(de.getMessage());
		 if (m.matches()) {
			 return m.group(1);
		 } else {
			 return "";
		 }
//		org.springframework.dao.DuplicateKeyException: jOOQ; SQL [insert into "PUBLIC"."USER_ACCOUNT" ("NAME", "MOBILE", "EMAIL", "DESCRIPTION", "CREATED_AT") values (cast(? as varchar(32672)), cast(? as varchar(32672)), cast(? as varchar(32672)), cast(? as varchar(32672)), cast(? as timestamp))integrity constraint violation: unique constraint or index violation; UNIQUE_UA_EMAIL table: USER_ACCOUNT; nested exception is java.sql.SQLIntegrityConstraintViolationException: integrity constraint violation: unique constraint or index violation; UNIQUE_UA_EMAIL table: USER_ACCOUNT
	}

}
