package com.go2wheel.mysqlbackup.value;

public class MsgKeyAndFills2 extends MsgKeyAndFillsBase {
	
	private String key;
	private Object o1;
	private Object o2;
	
	/**
	 * The empty value should be true, means there is no substitute values.
	 * @param k
	 */
	public MsgKeyAndFills2(String k) {
		super(true);
		this.key = k;
	}

	
	public MsgKeyAndFills2(String k, Object o1, Object o2) {
		super(false);
		this.key = k;
		this.o1 = o1;
		this.setO2(o2);
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


	public Object getO2() {
		return o2;
	}


	public void setO2(Object o2) {
		this.o2 = o2;
	}
}
