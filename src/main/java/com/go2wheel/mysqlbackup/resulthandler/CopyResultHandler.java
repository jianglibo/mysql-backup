package com.go2wheel.mysqlbackup.resulthandler;

import java.util.List;
import java.util.Map.Entry;

import org.springframework.shell.result.TerminalAwareResultHandler;

import com.go2wheel.mysqlbackup.value.CopyDescription;
import com.go2wheel.mysqlbackup.value.CopyDescription.COPY_STATE;
import com.go2wheel.mysqlbackup.value.CopyResult;

public class CopyResultHandler extends TerminalAwareResultHandler<CopyResult> {

	private CopyResult lastCopyResult; 
	
	@Override
	protected void doHandleResult(CopyResult result) {
		this.lastCopyResult = result;
		for(Entry<COPY_STATE, Long> entry: result.getCountMap().entrySet()) {
			terminal.writer().println(String.format("%s: %s", entry.getKey(), entry.getValue()));
		}
		if (result.isDetailed()) {
			for(Entry<COPY_STATE, List<CopyDescription>> entry: result.getDescriptionMap().entrySet()) {
				terminal.writer().println(entry.getKey() + ":");
				for(CopyDescription cd : entry.getValue()) {
					terminal.writer().println(cd.getSrcRelative());
				}
			}
		}
		terminal.writer().flush();
	}

	public CopyResult getLastCopyResult() {
		return lastCopyResult;
	}

}
