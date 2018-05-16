package com.go2wheel.mysqlbackup.resulthandler;


import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.NoSuchMessageException;
import org.springframework.shell.result.TerminalAwareResultHandler;

import com.go2wheel.mysqlbackup.ApplicationState;
import com.go2wheel.mysqlbackup.LocaledMessageService;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.value.FacadeResult;

public class FacadeResultHandler<T> extends TerminalAwareResultHandler<FacadeResult<T>> {


	@Autowired
	private LocaledMessageService messageService;
	
	@Autowired
	private ApplicationState applicationState;
	
	@Override
	protected void doHandleResult(FacadeResult<T> result) {
		applicationState.setFacadeResult(result);
		String msg;
		try {
			if (result.getException() != null) {
				msg = ExceptionUtil.stackTraceToString(result.getException());
			} else if(result.getResult() != null) {
				msg = result.getResult().toString();
			} else if (result.getMessage() != null && !result.getMessage().isEmpty()) {
				msg = messageService.getMessage(result.getMessage());
			} else {
				msg = "";
			}
		} catch (Exception e) {
			if (e instanceof NoSuchMessageException) {
				msg = String.format("Message key : %s doesn't exists.", result.getMessage());
			} else {
				msg = "An error occured. " + e.getMessage();
			}
		}
		terminal.writer().println(new AttributedStringBuilder()
				.append(msg, AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE)).toAnsi());
	}


}
