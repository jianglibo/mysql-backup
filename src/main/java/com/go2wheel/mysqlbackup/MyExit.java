package com.go2wheel.mysqlbackup;

import org.springframework.shell.standard.commands.Quit;
import org.springframework.shell.ExitRequest;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

//@ShellComponent
public class MyExit implements Quit.Command {
	
	/**
	 * Because this application started another web application runner. So even when this runner terminated, the application will keep running. 
	 */

	@ShellMethod(value = "Exit the shell.", key = {"quit", "exit"})
	public void quit() {
		throw new ExitRequest();
	}
	
}
