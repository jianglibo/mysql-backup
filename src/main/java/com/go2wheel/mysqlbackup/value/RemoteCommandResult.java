package com.go2wheel.mysqlbackup.value;

import java.util.Optional;

public class RemoteCommandResult<T> {
	
	private T result;
	
	private Optional<String> reason = Optional.empty();
	
	private int exitValue;
	
	private boolean success;
	
	public RemoteCommandResult() {}
	
	public RemoteCommandResult(T result, int exitValue) {
		this.result = result;
		this.exitValue = exitValue;
		this.success = true;
	}
	
	public static <T1> RemoteCommandResult<T1> failedResult(String reason) {
		RemoteCommandResult<T1> er = new RemoteCommandResult<>();
		er.reason = Optional.of(reason);
		er.success = false;
		return er;
	}
	
	@Override
	public String toString() {
		return String.format("[exitValue: %s, isSuccess: %s, reason: %s]", getExitValue(), isSuccess(), getReason());
	}

	public T getResult() {
		return result;
	}

	public void setResult(T result) {
		this.result = result;
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

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}
	
}
