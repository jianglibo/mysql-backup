package com.go2wheel.mysqlbackup.cfgoverrides.jlineshellautoconfig;

import java.util.Collections;

import org.jline.reader.LineReader;
import org.jline.reader.Parser;
import org.jline.reader.UserInterruptException;
import org.jline.utils.AttributedString;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.shell.ExitRequest;
import org.springframework.shell.Input;
import org.springframework.shell.InputProvider;
import org.springframework.shell.Shell;
import org.springframework.shell.jline.InteractiveShellApplicationRunner;
import org.springframework.shell.jline.PromptProvider;


/**
 * Shameful copy-paste of JLine's {@link org.springframework.shell.jline.InteractiveShellApplicationRunner}.
 * @author jianglibo@gmail.com
 *
 */
@Order(InteractiveShellApplicationRunner.PRECEDENCE)
public class InteractiveShellApplicationRunnerMine  implements ApplicationRunner {


	public static final String SPRING_SHELL_INTERACTIVE_ENABLED = "spring.shell.interactive";
	private final LineReader lineReader;

	private final PromptProvider promptProvider;

	@SuppressWarnings("unused")
	private final Parser parser;

	private final Shell shell;

	@SuppressWarnings("unused")
	private final Environment environment;

	public InteractiveShellApplicationRunnerMine(LineReader lineReader, PromptProvider promptProvider, Parser parser, Shell shell, Environment environment) {
		this.lineReader = lineReader;
		this.promptProvider = promptProvider;
		this.parser = parser;
		this.shell = shell;
		this.environment = environment;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		boolean interactive = isEnabled();
		if (interactive) {
		InputProvider inputProvider = new JLineInputProvider(lineReader, promptProvider);
			shell.run(inputProvider);
		}
	}
	
	public static void disable(ConfigurableEnvironment environment) {
		environment.getPropertySources().addFirst(new MapPropertySource("interactive.override",
				Collections.singletonMap(SPRING_SHELL_INTERACTIVE_ENABLED, "false")));
	}
	
//	public boolean isEnabled() {
//		return environment.getProperty(SPRING_SHELL_INTERACTIVE_ENABLED,boolean.class,  true);
//	}


	public static class JLineInputProvider implements InputProvider {

		private final LineReader lineReader;

		private final PromptProvider promptProvider;

		public JLineInputProvider(LineReader lineReader, PromptProvider promptProvider) {
			this.lineReader = lineReader;
			this.promptProvider = promptProvider;
		}

		@Override
		public Input readInput() {
			try {
				AttributedString prompt = promptProvider.getPrompt();
				lineReader.readLine(prompt.toAnsi(lineReader.getTerminal()));
			}
			catch (UserInterruptException e) {
				if (e.getPartialLine().isEmpty()) {
					throw new ExitRequest(1);
				} else {
					return Input.EMPTY;
				}
			}
			return new ParsedLineInputMine(lineReader.getParsedLine());
		}
	}


	public boolean isEnabled() {
		return true;
	}

}
