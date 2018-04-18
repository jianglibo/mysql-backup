package com.go2wheel.mysqlbackup.value;

public class ExecuteResult<T> {
	
	private T result;
	
	private boolean success;
	
	private String message;
	
	private String reason;
	
	public ExecuteResult() {}
	
	public ExecuteResult(T result) {
		this.result = result;
		this.success = true;
	}
	
	public static <T1> ExecuteResult<T1> failedResult(String reason) {
		ExecuteResult<T1> er = new ExecuteResult<>();
		er.setSuccess(false);
		er.reason = reason;
		return er;
	}

	public T getResult() {
		return result;
	}

	public void setResult(T result) {
		this.result = result;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getMessage() {
		return message;
	}

	public ExecuteResult<T> setMessage(String message) {
		this.message = message;
		return this;
	}
}
