package com.go2wheel.mysqlbackup.value;

public class FacadeResult<T> {
	
	private boolean expected;
	
	private T result;
	
	private CommonActionResult commonActionResult;
	
	private Exception exception;
	
	private String message;
	
	public static enum CommonActionResult {
		PREVIOUSLY_DONE, DONE
	}
	
	public static FacadeResult<?> unexpectedResult(Exception e) {
		FacadeResult<?> r = new FacadeResult<>();
		r.expected = false;
		r.setException(e);
		return r;
	}
	
	public static FacadeResult<?> unexpectedResult(String message) {
		FacadeResult<?> r = new FacadeResult<>();
		r.expected = false;
		r.setMessage(message);
		return r;
	}
	
	public static FacadeResult<?> doneResult() {
		FacadeResult<?> r = new FacadeResult<>();
		r.expected = true;
		r.setCommonActionResult(CommonActionResult.DONE);
		return r;
	}
	
	public static FacadeResult<?> doneCommonResult(CommonActionResult commonActionResult) {
		FacadeResult<?> r = new FacadeResult<>();
		r.expected = true;
		r.setCommonActionResult(commonActionResult);
		return r;
	}
	
	
	public static <T> FacadeResult<T> doneResult(T value) {
		FacadeResult<T> r = new FacadeResult<T>();
		r.expected = true;
		r.setResult(value);
		return r;
	}
	
	public FacadeResult() {
	}


	public boolean isExpected() {
		return expected;
	}

	public void setExpected(boolean expected) {
		this.expected = expected;
	}

	public Exception getException() {
		return exception;
	}


	public void setException(Exception exception) {
		this.exception = exception;
	}


	public CommonActionResult getCommonActionResult() {
		return commonActionResult;
	}


	public void setCommonActionResult(CommonActionResult commonActionResult) {
		this.commonActionResult = commonActionResult;
	}

	public T getResult() {
		return result;
	}

	public void setResult(T result) {
		this.result = result;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
