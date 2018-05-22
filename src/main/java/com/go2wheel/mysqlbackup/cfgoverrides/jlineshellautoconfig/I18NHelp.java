package com.go2wheel.mysqlbackup.cfgoverrides.jlineshellautoconfig;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.CommandValueProvider;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.standard.commands.Help;

@ShellComponent
public class I18NHelp {
	
	@Autowired
	private Help help;

	@ShellMethod(value = "Display help about available commands by Locale.", prefix = "-")
	public CharSequence helpI18n(@ShellOption(defaultValue = ShellOption.NULL, valueProvider = CommandValueProvider.class, value = { "-C",
	"--command" }, help = "The command to obtain help for.") String command) throws IOException {
		CharSequence cs = help.help(command); 
		return cs;
	}
}
