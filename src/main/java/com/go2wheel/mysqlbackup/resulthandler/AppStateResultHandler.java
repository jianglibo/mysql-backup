package com.go2wheel.mysqlbackup.resulthandler;

import java.util.ArrayList;
import java.util.List;

import org.springframework.shell.result.TerminalAwareResultHandler;

import com.go2wheel.mysqlbackup.ApplicationState;
import com.go2wheel.mysqlbackup.value.Box;

public class AppStateResultHandler extends TerminalAwareResultHandler<ApplicationState> {
	
	@Override
	protected void doHandleResult(ApplicationState appState) {
		if (appState.getServers().isEmpty()) {
			terminal.writer().println("尚未配置服务器。相关信息可以通过system-info命令查看。");
		} else {
			List<String> ss = new ArrayList<>();
			
			for(int i = 0; i < appState.getServers().size(); i++) {
				Box box = appState.getServers().get(i);
				String fs = "%s  %s";
				if (box.equals(appState.currentBoxOptional().orElse(null))) {
					fs = "*%s %s";
				}
				ss.add(String.format(fs, i, appState.getServers().get(i).getHost()));
			}
			ss.stream().forEach(terminal.writer()::println);
		}
		terminal.writer().flush();
	}

}
