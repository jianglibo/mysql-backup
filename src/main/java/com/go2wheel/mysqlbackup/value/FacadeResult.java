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
	
	public static <T> FacadeResult<T> unexpectedResult(Exception e) {
		FacadeResult<T> r = new FacadeResult<>();
		r.expected = false;
		r.setException(e);
		return r;
	}
	
	public static <T> FacadeResult<T> unexpectedResult(String message) {
		FacadeResult<T> r = new FacadeResult<>();
		r.expected = false;
		r.setMessage(message);
		return r;
	}
	
	public static <T> FacadeResult<T> unexpectedResult(Exception e, String message) {
		FacadeResult<T> r = new FacadeResult<>();
		r.expected = false;
		r.setMessage(message);
		r.setException(e);
		return r;
	}
	
	public static <T> FacadeResult<T> doneExpectedResult() {
		FacadeResult<T> r = new FacadeResult<>();
		r.expected = true;
		r.setCommonActionResult(CommonActionResult.DONE);
		r.setMessage("common.mission.accomplished");
		return r;
	}
	
	public static <T> FacadeResult<T> doneExpectedResult(CommonActionResult commonActionResult) {
		FacadeResult<T> r = new FacadeResult<>();
		r.expected = true;
		r.setCommonActionResult(commonActionResult);
		switch (commonActionResult) {
		case DONE:
			r.setMessage("common.mission.accomplished");
			break;
		case PREVIOUSLY_DONE:
			r.setMessage("common.mission.previousdone");
			break;
		default:
			break;
		}
		
		return r;
	}
	
	
	public static <T> FacadeResult<T> doneExpectedResult(T value, CommonActionResult commonActionResult) {
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