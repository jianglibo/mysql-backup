package com.go2wheel.mysqlbackup.exception;

public abstract class  HasErrorIdAndMsgkey extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String errorId;
	private final String msgkey;
	
	private final Object[] messagePlaceHolders;
	
	public HasErrorIdAndMsgkey(String errorId, String msgkey, String message, Object...messagePlaceHolders) {
		super(msgkey + "," + message);
		this.errorId = errorId;
		this.msgkey = msgkey;
		this.messagePlaceHolders = messagePlaceHolders;
	}

	public String getErrorId() {
		return errorId;
	}

	public String getMsgkey() {
		return msgkey;
	}

	public Object[] getMessagePlaceHolders() {
		return messagePlaceHolders;
	}
	
	
	
}
