package com.go2wheel.mysqlbackup.resulthandler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.CommandNotFound;
import org.springframework.shell.result.TerminalAwareResultHandler;


import com.go2wheel.mysqlbackup.ApplicationState;
import com.go2wheel.mysqlbackup.ApplicationState.CommandStepState;

public class MyThrowableResultHandler  extends TerminalAwareResultHandler<CommandNotFound> {
	
	private Pattern ptn = Pattern.compile("No command found for\\s+'(\\d+)'\\s*");
	
	@Autowired
	private ApplicationState appState;

	@Override
	protected void doHandleResult(CommandNotFound result) {
		String msg = result.getMessage();
		if (appState.getStep() == CommandStepState.WAITING_SELECT) {
			Matcher m = ptn.matcher(msg);
			if (m.matches()) {
				int i = Integer.valueOf(m.group(1));
				if (i != appState.getCurrentIndex() && i < appState.getServers().size()) {
					appState.setCurrentIndexAndFireEvent(i);
					appState.setStep(CommandStepState.BOX_SELECTED);
					msg = String.format("切换到新的服务器： %s", appState.currentBox().get().getHost());
				}
			}
		}

		terminal.writer().println(msg);
		terminal.writer().flush();
	}

}
