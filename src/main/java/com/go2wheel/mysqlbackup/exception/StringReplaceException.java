package com.go2wheel.mysqlbackup.exception;

public class StringReplaceException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String origin;
	private String pattern;
	
	private Object[] replacements;
	
	public StringReplaceException(String origin, String pattern, Object...replacements) {
		super(String.format("%s cannot match %s", pattern, origin));
		this.setOrigin(origin);
		this.pattern = pattern;
		this.setReplacements(replacements);
	}


	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}


	public String getOrigin() {
		return origin;
	}


	public void setOrigin(String origin) {
		this.origin = origin;
	}


	public Object[] getReplacements() {
		return replacements;
	}


	public void setReplacements(Object[] replacements) {
		this.replacements = replacements;
	}
	
	

}
