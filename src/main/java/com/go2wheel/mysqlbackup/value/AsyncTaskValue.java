package com.go2wheel.mysqlbackup.value;

public class AsyncTaskValue {
	
	private String description;
	
	private Object result;
	
	private boolean empty;
	
	public AsyncTaskValue() {}
	
	public AsyncTaskValue(Object result) {
		this.result = result;
		this.empty = false;
	}
	
	public static AsyncTaskValue emptyValue() {
		return new AsyncTaskValue();
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public AsyncTaskValue withDescription(String description) {
		setDescription(description);
		return this;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public boolean isEmpty() {
		return empty;
	}

	public void setEmpty(boolean empty) {
		this.empty = empty;
	}

}
