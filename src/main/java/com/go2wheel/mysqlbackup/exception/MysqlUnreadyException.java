package com.go2wheel.mysqlbackup.exception;

public class MysqlUnreadyException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String msgkey;
	
	public MysqlUnreadyException() {
		super("uncompleted mysql setup.");
		this.setMsgkey("mysql.unready");
	}

	public String getMsgkey() {
		return msgkey;
	}

	public void setMsgkey(String msgkey) {
		this.msgkey = msgkey;
	}

}
