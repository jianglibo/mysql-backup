package com.go2wheel.mysqlbackup.value;

import java.util.Optional;

public class ExternalExecuteResult<T> {
	
	private T result;
	
	private Optional<String> reason = Optional.empty();
	
	private int exitValue;
	
	public ExternalExecuteResult() {}
	
	public ExternalExecuteResult(T result, int exitValue) {
		this.result = result;
		this.exitValue = exitValue;
	}
	
	public static <T1> ExternalExecuteResult<T1> failedResult(String reason) {
		ExternalExecuteResult<T1> er = new ExternalExecuteResult<>();
		er.reason = Optional.of(reason);
		return er;
	}

	public T getResult() {
		return result;
	}

	public void setResult(T result) {
		this.result = result;
	}

	public boolean isSuccess() {
		return exitValue == 0 && !reason.isPresent();
	}


	public Optional<String> getReason() {
		return reason;
	}

	public int getExitValue() {
		return exitValue;
	}

	public void setExitValue(int exitValue) {
		this.exitValue = exitValue;
	}
	
}
