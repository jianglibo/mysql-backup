package com.go2wheel.mysqlbackup.resulthandler;

import java.util.stream.Collectors;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.shell.CommandRegistry;
import org.springframework.shell.ParameterMissingResolutionException;
import org.springframework.shell.UnfinishedParameterResolutionException;
import org.springframework.shell.result.ThrowableResultHandler;
import org.springframework.util.StringUtils;

import com.go2wheel.mysqlbackup.LocaledMessageService;
import com.go2wheel.mysqlbackup.cfgoverrides.jlineshellautoconfig.InteractiveShellApplicationRunnerMine;
import com.go2wheel.mysqlbackup.exception.ShowToUserException;
import com.go2wheel.mysqlbackup.value.CommonMessageKeys;

public class ThrowableResultHandlerMine extends ThrowableResultHandler implements ApplicationContextAware {

	/**
	 * The name of the command that may be used to print details about the last
	 * error.
	 */
	public static final String DETAILS_COMMAND_NAME = "stacktrace";

	private Throwable lastError;

	@Autowired
	@Lazy
	private CommandRegistry commandRegistry;

	@SuppressWarnings("unused")
	private ApplicationContext applicationContext;

	@Autowired
	private LocaledMessageService messageService;

	@Autowired
	@Lazy
	private InteractiveShellApplicationRunnerMine interactiveRunner;

	@Value("line.separator")
	private String lineSeparator;

	@Override
	protected void doHandleResult(Throwable result) {
		lastError = result;
		if (ShowToUserException.class.isAssignableFrom(result.getClass())) {
			String s;
			try {
				ShowToUserException stue = (ShowToUserException) result;
				s = messageService.getMessage(stue.getMessageKey(), stue.getMessagePlaceHolders());
			} catch (Exception e) {
				s = result.getMessage();
			}
			terminal.writer().println(new AttributedStringBuilder()
					.append(s, AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE)).toAnsi());
			return;
		} else if (result instanceof UnfinishedParameterResolutionException) {
			String s = ((UnfinishedParameterResolutionException) result).getParameterDescription().keys().stream()
					.map(k -> messageService.getMessage(CommonMessageKeys.PARAMETER_REQUIRED, k))
					.collect(Collectors.joining(lineSeparator));
			terminal.writer().println(new AttributedStringBuilder()
					.append(s, AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE)).toAnsi());
			return;
		} else if (result instanceof ParameterMissingResolutionException) {
			String s = ((ParameterMissingResolutionException) result).getParameterDescription().keys().stream()
					.map(k -> messageService.getMessage(CommonMessageKeys.PARAMETER_REQUIRED, k))
					.collect(Collectors.joining(lineSeparator));
			terminal.writer().println(new AttributedStringBuilder()
					.append(s, AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE)).toAnsi());
			return;
		} else if (result instanceof DuplicateKeyException) {
			DuplicateKeyException dke = (DuplicateKeyException) result;
			String s = messageService.getMessage(CommonMessageKeys.DB_DUPLICATE_KEY);
			terminal.writer().println(new AttributedStringBuilder()
					.append(s, AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE)).toAnsi());
			return;
		} else {
			String toPrint = StringUtils.hasLength(result.getMessage()) ? result.getMessage() : result.toString();
			terminal.writer().println(
					new AttributedString(toPrint, AttributedStyle.DEFAULT.foreground(AttributedStyle.RED)).toAnsi());
		}

		if (interactiveRunner.isEnabled() && commandRegistry.listCommands().containsKey(DETAILS_COMMAND_NAME)) {
			terminal.writer().println(new AttributedStringBuilder()
					.append("Details of the error have been omitted. You can use the ",
							AttributedStyle.DEFAULT.foreground(AttributedStyle.RED))
					.append(DETAILS_COMMAND_NAME, AttributedStyle.DEFAULT.foreground(AttributedStyle.RED).bold())
					.append(" command to print the full stacktrace.",
							AttributedStyle.DEFAULT.foreground(AttributedStyle.RED))
					.toAnsi());
		}
		terminal.writer().flush();
		if (!interactiveRunner.isEnabled()) {
			if (result instanceof RuntimeException) {
				throw (RuntimeException) result;
			} else if (result instanceof Error) {
				throw (Error) result;
			} else {
				throw new RuntimeException((Throwable) result);
			}
		}
	}

	/**
	 * Return the last error that was dealt with by this result handler.
	 */
	public Throwable getLastError() {
		return lastError;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
