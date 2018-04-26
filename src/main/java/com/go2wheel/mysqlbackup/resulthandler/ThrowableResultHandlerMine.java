package com.go2wheel.mysqlbackup.resulthandler;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.shell.CommandRegistry;
import org.springframework.shell.result.ThrowableResultHandler;
import org.springframework.util.StringUtils;

import com.go2wheel.mysqlbackup.ApplicationState;
import com.go2wheel.mysqlbackup.cfgoverrides.jlineshellautoconfig.InteractiveShellApplicationRunnerMine;

public class ThrowableResultHandlerMine  extends ThrowableResultHandler {

	/**
	 * The name of the command that may be used to print details about the last error.
	 */
	public static final String DETAILS_COMMAND_NAME = "stacktrace";

	private Throwable lastError;

	@Autowired @Lazy
	private CommandRegistry commandRegistry;

	@Autowired @Lazy
	private InteractiveShellApplicationRunnerMine interactiveRunner;

	@Override
	protected void doHandleResult(Throwable result) {
		lastError = result;
		String toPrint = StringUtils.hasLength(result.getMessage()) ? result.getMessage() : result.toString();
		terminal.writer().println(new AttributedString(toPrint,
				AttributedStyle.DEFAULT.foreground(AttributedStyle.RED)).toAnsi());
		if (interactiveRunner.isEnabled() && commandRegistry.listCommands().containsKey(DETAILS_COMMAND_NAME)) {
			terminal.writer().println(
				new AttributedStringBuilder()
					.append("Details of the error have been omitted. You can use the ", AttributedStyle.DEFAULT.foreground(AttributedStyle.RED))
					.append(DETAILS_COMMAND_NAME, AttributedStyle.DEFAULT.foreground(AttributedStyle.RED).bold())
					.append(" command to print the full stacktrace.", AttributedStyle.DEFAULT.foreground(AttributedStyle.RED))
					.toAnsi()
			);
		}
		terminal.writer().flush();
		if (!interactiveRunner.isEnabled()) {
			if (result instanceof RuntimeException) {
				throw (RuntimeException) result;
			}
			else if (result instanceof Error) {
				throw (Error) result;
			}
			else {
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
	
}
