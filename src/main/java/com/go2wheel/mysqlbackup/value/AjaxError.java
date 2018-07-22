package com.go2wheel.mysqlbackup.value;

import java.util.List;
import java.util.Map;

import org.assertj.core.util.Lists;

import com.google.common.collect.Maps;

public class AjaxError {
	
	private String message;
	
	private Map<String, List<ErrorItem>> errors = Maps.newHashMap();
	
	
	public AjaxError(String message) {
		this.message = message;
		this.errors.put("_global", Lists.newArrayList());
	}
	
	
	public static AjaxError getTimeOutError() {
		return new AjaxError("timeout");
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





//{
//    "message": "Validation Failed",
//    "errors": {
//        "title": [
//        {
//                    "code": "required_field",
//                    "message": "The title field is required."
//        },
//        {
//            "code": "max_field_error",
//            " message": "The title may not be greater than 50 characters."
//        }   
//    ],
//        "author":[
//        {
//                "code": "required_field",
//                "message": "The author field is required."
//            }
//    ]
//    }
//}