package com.go2wheel.mysqlbackup.value;

import com.go2wheel.mysqlbackup.util.StringUtil;

public class FacadeResult<T> {
	
	private boolean expected;
	
	private T result;
	
	private CommonActionResult commonActionResult;
	
	private Exception exception;
	
	private String message;
	
	public static enum CommonActionResult {
		PREVIOUSLY_DONE, DONE
	}
	
	public String getMixedMessage() {
		StringBuffer sb = new StringBuffer();
		if (isExpected()) {
			if (result != null) {
				sb.append(result.toString());
			}
			
			if (commonActionResult != null) {
				switch (commonActionResult) {
				case PREVIOUSLY_DONE:
					sb.append("发现任务已经完成， 什么都没做。");
					break;
				case DONE:
					sb.append("任务完成。");
				default:
					break;
				}
			}
			
		} else {
			if (exception != null) {
				sb.append(exception.getMessage()).append(", ");
			}
			if(StringUtil.hasAnyNonBlankWord(message)) {
				sb.append(message).append(", ");
			}
		}
		return sb.toString();
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
	
	public static <T> FacadeResult<T> doneResult() {
		FacadeResult<T> r = new FacadeResult<>();
		r.expected = true;
		r.setCommonActionResult(CommonActionResult.DONE);
		return r;
	}
	
	public static <T> FacadeResult<T> doneCommonResult(CommonActionResult commonActionResult) {
		FacadeResult<T> r = new FacadeResult<>();
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
