package com.go2wheel.mysqlbackup;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Test;

public class TestMySettingNoSpring {

	@Test
	public void t() throws IOException {
		MyAppSettings mas = UtilForTe.getMyAppSettings();
		assertNotNull(mas.getSsh().getKnownHosts());
		assertNotNull(mas.getSsh().getSshIdrsa());
	}

}
