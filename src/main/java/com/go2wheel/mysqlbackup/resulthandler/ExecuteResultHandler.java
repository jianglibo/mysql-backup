package com.go2wheel.mysqlbackup.resulthandler;

import org.springframework.shell.result.TerminalAwareResultHandler;

import com.go2wheel.mysqlbackup.value.ExecuteResult;

public class ExecuteResultHandler extends TerminalAwareResultHandler<ExecuteResult<?>> {

	@Override
	protected void doHandleResult(ExecuteResult<?> result) {
		System.out.println(result.getResult());
//		switch (result.getResultType()) {
//		case NO_RESULT:
//			terminal.writer().println("No copy result exists for now.");
//			break;
//		default:
//			for(Entry<COPY_STATE, Long> entry: result.getCountMap().entrySet()) {
//				terminal.writer().println(String.format("%s: %s", entry.getKey(), entry.getValue()));
//			}
//			if (result.getResultType() == CopyResultType.DETAILED) {
//				for(Entry<COPY_STATE, List<CopyDescription>> entry: result.getDescriptionMap().entrySet()) {
//					terminal.writer().println(entry.getKey() + ":");
//					for(CopyDescription cd : entry.getValue()) {
//						terminal.writer().println(cd.getSrcRelative());
//					}
//				}
//			}
//			break;
//		}
//		terminal.writer().flush();
	}

}
