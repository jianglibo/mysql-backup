package com.go2wheel.mysqlbackup.value;

public class MsgKeyAndFills1 extends MsgKeyAndFillsBase {
	
	private String key;
	private Object o1;
	
	/**
	 * The empty value should be true, means there is no substitute values.
	 * @param k
	 */
	public MsgKeyAndFills1(String k) {
		super(true);
		this.key = k;
	}

	
	public MsgKeyAndFills1(String k, Object o1) {
		super(false);
		this.key = k;
		this.o1 = o1;
	}
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public Object getO1() {
		return o1;
	}
	public void setO1(Object o1) {
		this.o1 = o1;
	}
}
