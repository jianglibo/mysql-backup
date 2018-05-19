package com.go2wheel.mysqlbackup.value;

import com.go2wheel.mysqlbackup.aop.TimeCost;

public class FacadeResult<T> implements TimeCost {
	
	private boolean expected;
	
	private T result;
	
	private CommonActionResult commonActionResult;
	
	private Exception exception;
	
	private String message;

	private Object[] messagePlaceHolders;
	
	private long startTime;
	
	private long endTime;
	
	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}


	public long getEndTime() {
		return endTime;
	}


	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public static enum CommonActionResult {
		PREVIOUSLY_DONE, DONE
	}
	
	public static <T> FacadeResult<T> showMessage(String message, Object...placeholders) {
		FacadeResult<T> r = new FacadeResult<>();
		r.expected = false;
		r.setMessage(message);
		r.messagePlaceHolders =  placeholders;
		return r;
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

	public static <T> FacadeResult<T> doneExpectedResultPreviousDone(String message) {
		FacadeResult<T> r = new FacadeResult<>();
		r.expected = true;
		r.setCommonActionResult(CommonActionResult.PREVIOUSLY_DONE);
		r.setMessage(message);
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

	public Object[] getMessagePlaceHolders() {
		return messagePlaceHolders;
	}

	public void setMessagePlaceHolders(Object[] messagePlaceHolders) {
		this.messagePlaceHolders = messagePlaceHolders;
	}
}
