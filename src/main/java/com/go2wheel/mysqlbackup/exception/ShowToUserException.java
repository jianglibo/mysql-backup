package com.go2wheel.mysqlbackup.exception;

public class ShowToUserException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final String messageKey;
	
	private Object[] messagePlaceHolders;
	
	public ShowToUserException(String messageKey, Object...messagePlaceHolders) {
		super(messageKey + ", placeholders: " + (messagePlaceHolders == null ? 0 : messagePlaceHolders.length));
		this.messageKey = messageKey;
		this.setMessagePlaceHolders(messagePlaceHolders);
	}

	public String getMessageKey() {
		return messageKey;
	}

	public Object[] getMessagePlaceHolders() {
		return messagePlaceHolders;
	}

	public void setMessagePlaceHolders(Object[] messagePlaceHolders) {
		this.messagePlaceHolders = messagePlaceHolders;
	}

}
