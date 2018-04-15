package com.go2wheel.mysqlbackup.tc;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.CommandNotFound;
import org.springframework.shell.result.TerminalAwareResultHandler;
import org.springframework.shell.result.ThrowableResultHandler;

import com.go2wheel.mysqlbackup.UtilForTe;

public class MyThrowableResultHandler  extends TerminalAwareResultHandler<CommandNotFound> {
	
	
//	private ThrowableResultHandler throwableResultHandler;
//	
//	@PostConstruct
//	public void setThrowableResultHandler(ThrowableResultHandler throwableResultHandler) {
//		this.throwableResultHandler = throwableResultHandler;
//	}

	@Override
	protected void doHandleResult(CommandNotFound result) {
		UtilForTe.printme(result.getMessage());
//		throwableResultHandler.handleResult(result);
	}

}
