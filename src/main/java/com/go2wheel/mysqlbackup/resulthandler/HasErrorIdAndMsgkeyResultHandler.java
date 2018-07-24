package com.go2wheel.mysqlbackup.resulthandler;

import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.shell.CommandRegistry;
import org.springframework.shell.result.TerminalAwareResultHandler;

import com.go2wheel.mysqlbackup.LocaledMessageService;
import com.go2wheel.mysqlbackup.exception.HasErrorIdAndMsgkey;

public class HasErrorIdAndMsgkeyResultHandler extends TerminalAwareResultHandler<HasErrorIdAndMsgkey>
		implements ApplicationContextAware {

	/**
	 * The name of the command that may be used to print details about the last
	 * error.
	 */
	public static final String DETAILS_COMMAND_NAME = "stacktrace";

	@Autowired
	@Lazy
	private CommandRegistry commandRegistry;

	@SuppressWarnings("unused")
	private ApplicationContext applicationContext;

	@Autowired
	private LocaledMessageService messageService;


	@Value("line.separator")
	private String lineSeparator;

	@Override
	protected void doHandleResult(HasErrorIdAndMsgkey result) {

		String s = null;
		try {
			s = messageService.getMessage(result.getMsgkey(), result.getMessagePlaceHolders());
			if (s == null) {
				s = result.getMessage();
			}
		} catch (Exception e) {
			s = result.getMessage();
		}
		if (s == null) s = "";
		terminal.writer().println(new AttributedStringBuilder()
				.append(s, AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE)).toAnsi());

		terminal.writer().flush();
		
//		boolean unHandled = SharedHandleMethods.handleCommonException(messageService, terminal, result);
//
//		if (unHandled) {
//			String toPrint = StringUtils.hasLength(result.getMessage()) ? result.getMessage() : result.toString();
//			terminal.writer().println(
//					new AttributedString(toPrint, AttributedStyle.DEFAULT.foreground(AttributedStyle.RED)).toAnsi());
//		}
//
//		if (interactiveRunner.isEnabled() && commandRegistry.listCommands().containsKey(DETAILS_COMMAND_NAME)) {
//			terminal.writer().println(new AttributedStringBuilder()
//					.append("Details of the error have been omitted. You can use the ",
//							AttributedStyle.DEFAULT.foreground(AttributedStyle.RED))
//					.append(DETAILS_COMMAND_NAME, AttributedStyle.DEFAULT.foreground(AttributedStyle.RED).bold())
//					.append(" command to print the full stacktrace.",
//							AttributedStyle.DEFAULT.foreground(AttributedStyle.RED))
//					.toAnsi());
//		}
//		terminal.writer().flush();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
