package com.go2wheel.mysqlbackup.value;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class AjaxErrorResult implements AjaxResult {

	private String message;
	
	private Map<String, List<ErrorItem>> errors = Maps.newHashMap();
	
	public static AjaxErrorResult exceptionResult(Throwable throwable) {
		AjaxErrorResult ar = new AjaxErrorResult(throwable.getMessage());
		return ar;
	}

	public static AjaxErrorResult errorResult(String message) {
		AjaxErrorResult ar = new AjaxErrorResult(message);
		return ar;
	}

	public AjaxErrorResult() {
		this.errors.put("_global", Lists.newArrayList());
	}

	public AjaxErrorResult(String message) {
		this.message = message;
		this.errors.put("_global", Lists.newArrayList());
	}
	
	
	public static AjaxErrorResult getTimeOutError() {
		return new AjaxErrorResult("timeout");
	}
	
	
	public void addGlobalErrorItem(String code, String message) {
		this.errors.get("_global").add(new ErrorItem(code, message));
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Map<String, List<ErrorItem>> getErrors() {
		return errors;
	}

	public void setErrors(Map<String, List<ErrorItem>> errors) {
		this.errors = errors;
	}

	public static class ErrorItem {
		private String code;
		private String message;
		
		public ErrorItem(String code, String message) {
			this.code = code;
			this.message = message;
		}
		public String getCode() {
			return code;
		}
		public void setCode(String code) {
			this.code = code;
		}
		public String getMessage() {
			return message;
		}
		public void setMessage(String message) {
			this.message = message;
		}
	}
}
