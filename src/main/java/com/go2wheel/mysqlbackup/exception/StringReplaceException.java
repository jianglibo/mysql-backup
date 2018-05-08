package com.go2wheel.mysqlbackup.exception;

import java.util.List;

public class StringReplaceException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String origin;
	private String pattern;
	
	private String[] replacements;
	
	public StringReplaceException(String origin, String pattern, String...replacements) {
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


	public String[] getReplacements() {
		return replacements;
	}


	public void setReplacements(String[] replacements) {
		this.replacements = replacements;
	}
	
	

}