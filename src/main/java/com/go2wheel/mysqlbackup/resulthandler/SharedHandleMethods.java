package com.go2wheel.mysqlbackup.resulthandler;

import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;

import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.shell.ParameterMissingResolutionException;
import org.springframework.shell.UnfinishedParameterResolutionException;

import com.go2wheel.mysqlbackup.LocaledMessageService;
import com.go2wheel.mysqlbackup.value.CommonMessageKeys;

public class SharedHandleMethods {

	/**
	 * 
	 * @param messageService
	 * @param terminal
	 * @param result
	 * @return unHandled boolean.
	 */
	public static boolean handleCommonException(LocaledMessageService messageService, Terminal terminal,
			Throwable result) {

		if (result instanceof UnfinishedParameterResolutionException) {
			String s = ((UnfinishedParameterResolutionException) result).getParameterDescription().keys().stream()
					.map(k -> messageService.getMessage(CommonMessageKeys.PARAMETER_UNFINISHED, k))
					.collect(Collectors.joining("\n"));
			terminal.writer().println(new AttributedStringBuilder()
					.append(s, AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE)).toAnsi());
		} else if (result instanceof ParameterMissingResolutionException) {
			String s = ((ParameterMissingResolutionException) result).getParameterDescription().keys().stream()
					.map(k -> messageService.getMessage(CommonMessageKeys.PARAMETER_REQUIRED, k))
					.collect(Collectors.joining("\n"));
			terminal.writer().println(new AttributedStringBuilder()
					.append(s, AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE)).toAnsi());
		} else if (result instanceof DuplicateKeyException) {
			DuplicateKeyException dke = (DuplicateKeyException) result;
			String s = messageService.getMessage(CommonMessageKeys.DB_DUPLICATE_KEY);
			terminal.writer().println(new AttributedStringBuilder()
					.append(s, AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE)).toAnsi());
		} else if (result instanceof ConstraintViolationException) {
			ConstraintViolationException dke = (ConstraintViolationException) result;
			String vv = dke.getConstraintViolations().stream().map(v -> v.getInvalidValue().toString())
					.collect(Collectors.joining(", "));
			String s = messageService.getMessage(CommonMessageKeys.MALFORMED_VALUE, vv);
			terminal.writer().println(new AttributedStringBuilder()
					.append(s, AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE)).toAnsi());
		} else {
			return true;
		}
		
		return false;
	}

}
