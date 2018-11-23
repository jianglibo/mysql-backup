package com.go2wheel.mysqlbackup.resulthandler;


import java.util.concurrent.TimeUnit;

import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.NoSuchMessageException;
import org.springframework.shell.result.TerminalAwareResultHandler;

import com.go2wheel.mysqlbackup.AppEventListenerBean;
import com.go2wheel.mysqlbackup.LocaledMessageService;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.value.FacadeResult;

public class FacadeResultHandler<T> extends TerminalAwareResultHandler<FacadeResult<T>> {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private LocaledMessageService messageService;
	
	@Autowired
	private AppEventListenerBean applicationState;
	
	@Override
	protected void doHandleResult(FacadeResult<T> result) {
		try {
			applicationState.setFacadeResult(result);
			String msg = "";
			
			T resultValue = result.getResult();
			try {
				if (result.getException() != null) {
					boolean unHandled = SharedHandleMethods.handleCommonException(messageService, terminal, result.getException());
					if (unHandled) {
						msg = ExceptionUtil.stackTraceToString(result.getException());
					} else {
						return;
					}
				} else if(resultValue != null) {
					msg = result.resultToString();
				} else if (result.getMessage() != null && !result.getMessage().isEmpty()) {
					msg = messageService.getMessage(result.getMessage(), result.getMessagePlaceHolders());
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
			
			if (result.getStartTime() > 0 ) {
				msg = String.format("startTime: %s, timeCosts: %s\n", new java.util.Date(result.getStartTime()), result.getTimeCost(TimeUnit.MILLISECONDS)) + msg;
			}
			terminal.writer().println(new AttributedStringBuilder()
					.append(msg, AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE)).toAnsi());
		} catch (Exception e) {
			ExceptionUtil.logErrorException(logger, e);
		}
	}
}
