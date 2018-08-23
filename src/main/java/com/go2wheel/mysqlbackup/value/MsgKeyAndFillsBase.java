package com.go2wheel.mysqlbackup.value;

public class MsgKeyAndFillsBase {
	
	private boolean empty;
	
	public MsgKeyAndFillsBase(boolean empty) {
		this.empty = empty;
	}

	public boolean isEmpty() {
		return empty;
	}

	public void setEmpty(boolean empty) {
		this.empty = empty;
	}

}
