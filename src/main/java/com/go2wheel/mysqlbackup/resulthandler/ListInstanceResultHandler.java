package com.go2wheel.mysqlbackup.resulthandler;

import java.util.ArrayList;
import java.util.List;

import org.springframework.shell.result.TerminalAwareResultHandler;

import com.go2wheel.mysqlbackup.value.ListInstanceResult;

public class ListInstanceResultHandler extends TerminalAwareResultHandler<ListInstanceResult> {

	@Override
	protected void doHandleResult(ListInstanceResult result) {
		if (result.isSuccess()) {
			if (result.getResult() == null || result.getResult().isEmpty()) {
				terminal.writer().println("No instance exists. Please use create-instance command to create one.");
			} else {
				List<String> ss = new ArrayList<>();
				for(int i = 0; i < result.getResult().size(); i++) {
					ss.add(String.format("%s  %s", i, result.getResult().get(i).getFileName()));
				}
				ss.stream().forEach(terminal.writer()::println);
			}
		} else {
			terminal.writer().println(result.getReason());
		}
		terminal.writer().flush();
	}

}
