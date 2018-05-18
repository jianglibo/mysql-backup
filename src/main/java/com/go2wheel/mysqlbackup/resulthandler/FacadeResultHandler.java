package com.go2wheel.mysqlbackup.resulthandler;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.NoSuchMessageException;
import org.springframework.shell.result.TerminalAwareResultHandler;

import com.go2wheel.mysqlbackup.ApplicationState;
import com.go2wheel.mysqlbackup.LocaledMessageService;
import com.go2wheel.mysqlbackup.util.ExceptionUtil;
import com.go2wheel.mysqlbackup.value.FacadeResult;

public class FacadeResultHandler<T> extends TerminalAwareResultHandler<FacadeResult<T>> {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private LocaledMessageService messageService;
	
	@Autowired
	private ApplicationState applicationState;
	
	@Override
	protected void doHandleResult(FacadeResult<T> result) {
		try {
			applicationState.setFacadeResult(result);
			String msg = "";
			T resultValue = result.getResult();
			try {
				if (result.getException() != null) {
					msg = ExceptionUtil.stackTraceToString(result.getException());
				} else if(result.getResult() != null) {
					if (resultValue instanceof Collection) {
						msg = handleCollection((Collection<?>) resultValue);
					} else if (resultValue instanceof Map) {
						msg = handleMap((Map<?, ?>) resultValue);
					} else {
						msg = resultValue.toString();
					}
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
		} catch (Exception e) {
			ExceptionUtil.logErrorException(logger, e);
		}
	}

	private String handleMap(Map<?, ?> result) {
		String msg = "";
		if (result.size() > 0) {
			List<String> ls = new ArrayList<>();
			
			for(Entry<?, ?> entry: result.entrySet()) {
				ls.add(String.format("%s: %s", entry.getKey().toString(), entry.getValue().toString()));
			}
			msg = String.join("\n", ls);
		}
		return msg;
	}

	private String handleCollection(Collection<?> result) {
		String msg = "";
		if (result.size() > 0) {
			List<String> ls = (List<String>) result.stream().map(o -> o.toString()).collect(Collectors.toList());
			msg = String.join("\n", ls);
		}
		return msg;
	}


}
