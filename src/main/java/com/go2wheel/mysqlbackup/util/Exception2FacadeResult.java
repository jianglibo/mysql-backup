package com.go2wheel.mysqlbackup.util;

import com.go2wheel.mysqlbackup.value.FacadeResult;
import com.jcraft.jsch.JSchException;

public class Exception2FacadeResult {

	
	public static FacadeResult<?> parseException(JSchException e) {
		FacadeResult<?> frSession = null;
		if (e.getMessage().contains("Auth fail")) {
			frSession = FacadeResult.unexpectedResult(e, "jsch.connect.authfailed");
		} else if (e.getMessage().contains("Connection timed out")) {
			frSession = FacadeResult.unexpectedResult(e, "jsch.connect.failed");
		} else {
			frSession = FacadeResult.unexpectedResult(e, "jsch.connect.failed");
		}
		return frSession;
	}
}
