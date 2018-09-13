package com.go2wheel.mysqlbackup.exception;

import org.junit.Test;

public class TestRemoteFileNotAbsoluteException {

	@Test(expected = RemoteFileNotAbsoluteException.class)
	public void tthrow() {
		RemoteFileNotAbsoluteException.throwIfNeed("ac");
	}
	
	@Test
	public void tNotThrowWinStyle() {
		RemoteFileNotAbsoluteException.throwIfNeed("c:\\ac\\a");
	}
	
	@Test
	public void tNotThrowWinStyle1() {
		RemoteFileNotAbsoluteException.throwIfNeed("c:/ac");
	}
	
	@Test
	public void tNotThrowWinStyle2() {
		RemoteFileNotAbsoluteException.throwIfNeed("z:/ac");
	}

	@Test
	public void tMysqlIni() {
		RemoteFileNotAbsoluteException.throwIfNeed("E:\\wamp64\\bin\\mysql\\mysql5.7.21\\my.ini");
	}

	
	@Test
	public void tNotTTrowLinuxStyle() {
		RemoteFileNotAbsoluteException.throwIfNeed("/ac");
	}


}
