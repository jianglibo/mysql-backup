package com.go2wheel.mysqlbackup;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class TestMySettingNoSpring {

	@Test
	public void t() {
		MyAppSettings mas = UtilForTe.getMyAppSettings();
		assertNotNull(mas.getSsh().getKnownHosts());
		assertNotNull(mas.getSsh().getSshIdrsa());
	}

}
