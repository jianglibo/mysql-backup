package com.go2wheel.mysqlbackup.value;

public class OsTypeWrapper {
	
	private final String os;
	
	
	public static OsTypeWrapper of(String os) {
		return new OsTypeWrapper(os != null ? os : "");
	}
	
	private OsTypeWrapper(String os) {
		this.os = os.toUpperCase();
	}
	
	public boolean isWin() {
		return os.startsWith("WIN");
	}
	
	public boolean isWin2008() {
		return os.startsWith("WIN_2008"); 
	}
	
	public boolean isWin2012() {
		return os.startsWith("WIN_2012"); 
	}
	
	public boolean isWin10() {
		return os.startsWith("WIN_10"); 
	}

}
